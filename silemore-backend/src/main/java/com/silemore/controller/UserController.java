package com.silemore.controller;

import com.silemore.config.UserPrincipal;
import com.silemore.dto.ApiResponse;
import com.silemore.dto.PauseRequest;
import com.silemore.dto.PauseResponse;
import com.silemore.dto.SettingsResponse;
import com.silemore.dto.UpdateProfileRequest;
import com.silemore.dto.UpdateProfileResponse;
import com.silemore.dto.UpdateSettingsRequest;
import com.silemore.dto.UserProfileResponse;
import com.silemore.entity.CheckIn;
import com.silemore.entity.User;
import com.silemore.exception.AppException;
import com.silemore.service.CheckInService;
import com.silemore.service.UserService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final CheckInService checkInService;

    public UserController(UserService userService, CheckInService checkInService) {
        this.userService = userService;
        this.checkInService = checkInService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getUser(principal.getUserId());
        long totalCheckIns = userService.getTotalCheckIns(user.getId());
        int currentStreak = checkInService.calculateStreakDays(user.getId());
        int longestStreak = checkInService.calculateLongestStreak(user.getId());
        Optional<CheckIn> lastCheckIn = userService.getLastCheckIn(user.getId());
        LocalDateTime lastCheckInAt = lastCheckIn.map(CheckIn::getCheckInTime).orElse(null);

        UserProfileResponse.Stats stats = new UserProfileResponse.Stats(
                totalCheckIns,
                currentStreak,
                longestStreak,
                lastCheckInAt
        );

        UserProfileResponse.Contacts contacts = new UserProfileResponse.Contacts(
                userService.getContactTotal(user.getId()),
                userService.getVerifiedContactTotal(user.getId())
        );

        LocalDateTime pauseUntil = user.getIsPaused() ? user.getPauseUntil() : null;
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAlertDays(),
                user.getReminderTime().toString(),
                user.getReminderEnabled(),
                user.getIsPaused(),
                pauseUntil,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                stats,
                contacts
        );

        return ApiResponse.success(response);
    }

    @PatchMapping("/me/settings")
    public ApiResponse<SettingsResponse> updateSettings(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody UpdateSettingsRequest request) {
        User user = userService.getUser(principal.getUserId());
        User updated = userService.updateSettings(user, request);
        LocalDateTime pauseUntil = updated.getIsPaused() ? updated.getPauseUntil() : null;
        SettingsResponse response = new SettingsResponse(
                updated.getAlertDays(),
                updated.getReminderTime().toString(),
                updated.getReminderEnabled(),
                updated.getIsPaused(),
                pauseUntil
        );
        return ApiResponse.success(response);
    }

    @PatchMapping("/me")
    public ApiResponse<UpdateProfileResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.getUser(principal.getUserId());
        User updated = userService.updateProfile(user, request.nickname());
        UpdateProfileResponse response = new UpdateProfileResponse(
                updated.getId(),
                updated.getEmail(),
                updated.getNickname(),
                updated.getUpdatedAt()
        );
        return ApiResponse.success(response);
    }

    @PostMapping("/me/pause")
    public ApiResponse<PauseResponse> pause(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody PauseRequest request) {
        User user = userService.getUser(principal.getUserId());
        String action = request.action();
        if (action == null) {
            throw new AppException(400, HttpStatus.BAD_REQUEST, "Action is required");
        }
        if ("pause".equalsIgnoreCase(action)) {
            if (request.duration() == null || request.duration() < 1 || request.duration() > 30) {
                throw new AppException(400, HttpStatus.BAD_REQUEST, "Duration must be 1-30 days");
            }
            User paused = userService.pause(user, request.duration());
            return ApiResponse.success(new PauseResponse(true, paused.getPauseUntil(), request.reason()));
        }
        if ("resume".equalsIgnoreCase(action)) {
            userService.resume(user);
            return ApiResponse.success(new PauseResponse(false, null, null));
        }
        throw new AppException(400, HttpStatus.BAD_REQUEST, "Invalid action");
    }
}
