package com.silemore.service;

import com.silemore.dto.UpdateSettingsRequest;
import com.silemore.entity.CheckIn;
import com.silemore.entity.User;
import com.silemore.exception.AppException;
import com.silemore.repository.CheckInRepository;
import com.silemore.repository.EmergencyContactRepository;
import com.silemore.repository.UserRepository;
import com.silemore.util.TimeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final EmergencyContactRepository contactRepository;

    public UserService(UserRepository userRepository,
                       CheckInRepository checkInRepository,
                       EmergencyContactRepository contactRepository) {
        this.userRepository = userRepository;
        this.checkInRepository = checkInRepository;
        this.contactRepository = contactRepository;
    }

    public User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(404, HttpStatus.NOT_FOUND, "User not found"));
        return normalizePauseIfExpired(user);
    }

    public User updateSettings(User user, UpdateSettingsRequest request) {
        if (request.alertDays() != null) {
            user.setAlertDays(request.alertDays());
        }
        if (request.reminderTime() != null && !request.reminderTime().isBlank()) {
            user.setReminderTime(LocalTime.parse(request.reminderTime()));
        }
        if (request.reminderEnabled() != null) {
            user.setReminderEnabled(request.reminderEnabled());
        }
        return userRepository.save(user);
    }

    public User updateProfile(User user, String nickname) {
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    public User pause(User user, int durationDays) {
        LocalDateTime until = TimeUtil.today().plusDays(durationDays).atTime(23, 59, 59);
        user.setIsPaused(true);
        if (user.getPauseStartedAt() == null) {
            user.setPauseStartedAt(TimeUtil.now());
        }
        user.setPauseUntil(until);
        return userRepository.save(user);
    }

    public User resume(User user) {
        if (user.getPauseStartedAt() != null) {
            LocalDateTime end = TimeUtil.now();
            if (user.getPauseUntil() != null && user.getPauseUntil().isBefore(end)) {
                end = user.getPauseUntil();
            }
            addPausedDays(user, end);
        }
        user.setIsPaused(false);
        user.setPauseUntil(null);
        user.setPauseStartedAt(null);
        return userRepository.save(user);
    }

    public User normalizePauseIfExpired(User user) {
        if (Boolean.TRUE.equals(user.getIsPaused())
                && user.getPauseUntil() != null
                && TimeUtil.now().isAfter(user.getPauseUntil())) {
            if (user.getPauseStartedAt() != null) {
                addPausedDays(user, user.getPauseUntil());
            }
            user.setIsPaused(false);
            user.setPauseUntil(null);
            user.setPauseStartedAt(null);
            return userRepository.save(user);
        }
        return user;
    }

    public long getTotalCheckIns(Long userId) {
        return checkInRepository.countByUserId(userId);
    }

    public Optional<CheckIn> getLastCheckIn(Long userId) {
        return checkInRepository.findTopByUserIdOrderByCheckInDateDesc(userId);
    }

    public int getContactTotal(Long userId) {
        return contactRepository.findByUserId(userId).size();
    }

    public int getVerifiedContactTotal(Long userId) {
        return contactRepository.findByUserIdAndIsVerifiedTrue(userId).size();
    }

    private void addPausedDays(User user, LocalDateTime pauseEnd) {
        LocalDateTime pauseStart = user.getPauseStartedAt();
        if (pauseStart == null || pauseEnd == null || pauseEnd.isBefore(pauseStart)) {
            return;
        }
        LocalDate startDate = pauseStart.toLocalDate();
        LocalDate endDate = pauseEnd.toLocalDate();
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int current = user.getPausedDaysSinceLastCheckIn() == null ? 0 : user.getPausedDaysSinceLastCheckIn();
        user.setPausedDaysSinceLastCheckIn(current + (int) days);
    }
}
