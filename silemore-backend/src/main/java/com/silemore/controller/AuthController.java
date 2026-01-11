package com.silemore.controller;

import com.silemore.dto.ApiResponse;
import com.silemore.dto.CheckInTodayResponse;
import com.silemore.dto.LoginRequest;
import com.silemore.dto.LoginResponse;
import com.silemore.dto.RegisterRequest;
import com.silemore.dto.RegisterResponse;
import com.silemore.entity.User;
import com.silemore.service.AuthService;
import com.silemore.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class AuthController {
    private final AuthService authService;
    private final CheckInService checkInService;

    public AuthController(AuthService authService, CheckInService checkInService) {
        this.authService = authService;
        this.checkInService = checkInService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Registered", response));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);
        User user = result.user();
        CheckInTodayResponse status = checkInService.getTodayStatus(user);
        int streakDays = status.stats().currentStreak();

        LoginResponse.LoginUser loginUser = new LoginResponse.LoginUser(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAlertDays(),
                user.getReminderTime().toString(),
                user.getIsPaused(),
                status.hasCheckedIn(),
                streakDays
        );

        LoginResponse response = new LoginResponse(
                result.token(),
                "Bearer",
                result.expiresIn(),
                loginUser
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
