package com.silemore.service;

import com.silemore.dto.CheckInHistoryResponse;
import com.silemore.dto.CheckInResponse;
import com.silemore.dto.CheckInTodayResponse;
import com.silemore.entity.CheckIn;
import com.silemore.entity.EmergencyContact;
import com.silemore.entity.NotificationStatus;
import com.silemore.entity.NotificationType;
import com.silemore.entity.User;
import com.silemore.exception.AppException;
import com.silemore.repository.CheckInRepository;
import com.silemore.repository.EmergencyContactRepository;
import com.silemore.repository.UserRepository;
import com.silemore.util.TimeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckInService {
    private final CheckInRepository checkInRepository;
    private final EmergencyContactRepository contactRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public CheckInService(CheckInRepository checkInRepository,
                          EmergencyContactRepository contactRepository,
                          UserRepository userRepository,
                          EmailService emailService,
                          NotificationService notificationService) {
        this.checkInRepository = checkInRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @Transactional
    public CheckInResponse checkIn(User user) {
        LocalDate today = TimeUtil.today();
        if (checkInRepository.existsByUserIdAndCheckInDate(user.getId(), today)) {
            throw new AppException(20001, HttpStatus.CONFLICT, "Already checked in today");
        }

        long missedDaysBefore = calculateMissedDays(user);

        CheckIn checkIn = new CheckIn();
        checkIn.setUser(user);
        checkIn.setCheckInDate(today);
        checkIn.setCheckInTime(TimeUtil.now());
        CheckIn saved;
        try {
            saved = checkInRepository.save(checkIn);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(20001, HttpStatus.CONFLICT, "Already checked in today");
        }

        user.setPausedDaysSinceLastCheckIn(0);
        if (Boolean.TRUE.equals(user.getIsPaused())) {
            user.setPauseStartedAt(TimeUtil.now());
        } else {
            user.setPauseStartedAt(null);
            user.setPauseUntil(null);
        }
        userRepository.save(user);

        int streakDays = calculateStreakDays(user.getId());

        if (missedDaysBefore >= user.getAlertDays()) {
            List<EmergencyContact> contacts = contactRepository.findByUserIdAndIsVerifiedTrue(user.getId());
            for (EmergencyContact contact : contacts) {
                try {
                    emailService.sendRecoveryEmail(contact, user);
                    notificationService.saveNotification(user, contact, NotificationType.RECOVERY,
                            NotificationStatus.SENT, null);
                } catch (Exception ex) {
                    notificationService.saveNotification(user, contact, NotificationType.RECOVERY,
                            NotificationStatus.FAILED, ex.getMessage());
                }
            }
        }

        return new CheckInResponse(
                saved.getId(),
                saved.getCheckInDate(),
                saved.getCheckInTime(),
                streakDays,
                false,
                "Keep it up!"
        );
    }

    public CheckInTodayResponse getTodayStatus(User user) {
        LocalDate today = TimeUtil.today();
        Optional<CheckIn> todayCheckIn = checkInRepository.findByUserIdAndCheckInDate(user.getId(), today);

        boolean hasCheckedIn = todayCheckIn.isPresent();
        CheckInTodayResponse.CheckInItem item = todayCheckIn
                .map(ci -> new CheckInTodayResponse.CheckInItem(ci.getId(), ci.getCheckInDate(), ci.getCheckInTime()))
                .orElse(null);

        int currentStreak = calculateStreakDays(user.getId());
        long missedDays = calculateMissedDays(user);
        LocalDateTime lastCheckInAt = getLastCheckIn(user.getId()).map(CheckIn::getCheckInTime).orElse(null);

        CheckInTodayResponse.Stats stats = new CheckInTodayResponse.Stats(
                currentStreak,
                missedDays,
                user.getAlertDays(),
                lastCheckInAt
        );

        return new CheckInTodayResponse(hasCheckedIn, item, stats);
    }

    public CheckInHistoryResponse getHistory(User user, int page, int size,
                                             LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CheckIn> results;
        if (startDate != null && endDate != null) {
            results = checkInRepository.findByUserIdAndCheckInDateBetween(user.getId(), startDate, endDate, pageable);
        } else {
            results = checkInRepository.findByUserId(user.getId(), pageable);
        }

        List<CheckInHistoryResponse.CheckInHistoryItem> items = results.getContent().stream()
                .map(ci -> new CheckInHistoryResponse.CheckInHistoryItem(
                        ci.getId(),
                        ci.getCheckInDate(),
                        ci.getCheckInTime()))
                .collect(Collectors.toList());

        return new CheckInHistoryResponse(items, results.getNumber(), results.getSize(),
                results.getTotalElements(), results.getTotalPages());
    }

    public int calculateLongestStreak(Long userId) {
        List<CheckIn> checkIns = checkInRepository.findByUserIdOrderByCheckInDateDesc(userId);
        if (checkIns.isEmpty()) {
            return 0;
        }

        int longest = 0;
        int current = 0;
        LocalDate previousDate = null;

        for (CheckIn checkIn : checkIns) {
            LocalDate date = checkIn.getCheckInDate();
            if (previousDate == null || date.plusDays(1).equals(previousDate)) {
                current++;
            } else {
                current = 1;
            }
            if (current > longest) {
                longest = current;
            }
            previousDate = date;
        }

        return longest;
    }

    public int calculateStreakDays(Long userId) {
        Optional<CheckIn> lastCheckIn = getLastCheckIn(userId);
        if (lastCheckIn.isEmpty()) {
            return 0;
        }

        LocalDate checkDate = lastCheckIn.get().getCheckInDate();
        int streak = 0;

        while (true) {
            boolean exists = checkInRepository.existsByUserIdAndCheckInDate(userId, checkDate);
            if (exists) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public long calculateMissedDays(User user) {
        if (Boolean.TRUE.equals(user.getIsPaused()) && user.getPauseUntil() != null
                && TimeUtil.now().isBefore(user.getPauseUntil())) {
            return 0;
        }

        LocalDate lastCheckIn = getLastCheckIn(user.getId())
                .map(CheckIn::getCheckInDate)
                .orElse(user.getCreatedAt().toLocalDate());

        LocalDate baseline = lastCheckIn;
        long missedDays = ChronoUnit.DAYS.between(baseline, TimeUtil.today());

        int pausedDays = user.getPausedDaysSinceLastCheckIn() == null ? 0 : user.getPausedDaysSinceLastCheckIn();

        if (Boolean.TRUE.equals(user.getIsPaused())
                && user.getPauseUntil() != null
                && TimeUtil.now().isBefore(user.getPauseUntil())) {
            return 0;
        }

        if (user.getPauseStartedAt() != null && user.getPauseUntil() != null
                && TimeUtil.now().isAfter(user.getPauseUntil())) {
            LocalDate pauseStart = user.getPauseStartedAt().toLocalDate();
            LocalDate pauseEnd = user.getPauseUntil().toLocalDate();
            long extra = ChronoUnit.DAYS.between(pauseStart, pauseEnd) + 1;
            pausedDays += (int) extra;
        }

        return Math.max(0, missedDays - pausedDays);
    }

    public Optional<CheckIn> getLastCheckIn(Long userId) {
        return checkInRepository.findTopByUserIdOrderByCheckInDateDesc(userId);
    }

    public boolean hasCheckedInToday(Long userId) {
        return checkInRepository.existsByUserIdAndCheckInDate(userId, TimeUtil.today());
    }
}
