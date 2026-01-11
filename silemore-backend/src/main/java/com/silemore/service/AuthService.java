package com.silemore.service;

import com.silemore.dto.LoginRequest;
import com.silemore.dto.RegisterRequest;
import com.silemore.entity.User;
import com.silemore.exception.AppException;
import com.silemore.repository.UserRepository;
import com.silemore.util.JwtUtil;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public record AuthResult(String token, long expiresIn, User user) {
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public User register(RegisterRequest request) {
        if (!Boolean.TRUE.equals(request.agreeTerms())) {
            throw new AppException(400, HttpStatus.BAD_REQUEST, "Terms must be accepted");
        }

        Optional<User> existing = userRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            throw new AppException(10001, HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setAlertDays(3);
        user.setReminderTime(LocalTime.of(20, 0));
        user.setReminderEnabled(true);
        user.setIsPaused(false);
        user.setPauseUntil(null);
        user.setPausedDaysSinceLastCheckIn(0);
        User saved = userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(saved);
        } catch (Exception ignored) {
            // Email is best-effort for MVP.
        }

        return saved;
    }

    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(10002, HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(10002, HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        boolean rememberMe = Boolean.TRUE.equals(request.rememberMe());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), rememberMe);
        long expiresIn = jwtUtil.getExpirationSeconds(rememberMe);
        return new AuthResult(token, expiresIn, user);
    }
}
