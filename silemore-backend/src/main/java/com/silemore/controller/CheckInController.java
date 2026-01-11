package com.silemore.controller;

import com.silemore.config.UserPrincipal;
import com.silemore.dto.ApiResponse;
import com.silemore.dto.CheckInHistoryResponse;
import com.silemore.dto.CheckInResponse;
import com.silemore.dto.CheckInTodayResponse;
import com.silemore.entity.User;
import com.silemore.exception.AppException;
import com.silemore.service.CheckInService;
import com.silemore.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/check-ins")
public class CheckInController {
    private final CheckInService checkInService;
    private final UserService userService;

    public CheckInController(CheckInService checkInService, UserService userService) {
        this.checkInService = checkInService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getUser(principal.getUserId());
        CheckInResponse response = checkInService.checkIn(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Checked in", response));
    }

    @GetMapping("/today")
    public ApiResponse<CheckInTodayResponse> today(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getUser(principal.getUserId());
        return ApiResponse.success(checkInService.getTodayStatus(user));
    }

    @GetMapping
    public ApiResponse<CheckInHistoryResponse> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new AppException(400, HttpStatus.BAD_REQUEST, "startDate and endDate must be together");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new AppException(400, HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }

        User user = userService.getUser(principal.getUserId());
        return ApiResponse.success(checkInService.getHistory(user, page, size, startDate, endDate));
    }
}
