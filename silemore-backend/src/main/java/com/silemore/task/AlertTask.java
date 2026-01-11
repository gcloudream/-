package com.silemore.task;

import com.silemore.entity.CheckIn;
import com.silemore.entity.EmergencyContact;
import com.silemore.entity.NotificationStatus;
import com.silemore.entity.NotificationType;
import com.silemore.entity.User;
import com.silemore.repository.EmergencyContactRepository;
import com.silemore.repository.UserRepository;
import com.silemore.service.CheckInService;
import com.silemore.service.EmailService;
import com.silemore.service.NotificationService;
import com.silemore.service.UserService;
import com.silemore.util.TimeUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertTask {
    private static final int MAX_ALERTS = 7;

    private final UserRepository userRepository;
    private final EmergencyContactRepository contactRepository;
    private final CheckInService checkInService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserService userService;

    public AlertTask(UserRepository userRepository,
                     EmergencyContactRepository contactRepository,
                     CheckInService checkInService,
                     EmailService emailService,
                     NotificationService notificationService,
                     UserService userService) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.checkInService = checkInService;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Scheduled(cron = "0 30 0 * * ?")
    public void checkMissedCheckIns() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user = userService.normalizePauseIfExpired(user);
            if (Boolean.TRUE.equals(user.getIsPaused())) {
                continue;
            }

            long missedDays = checkInService.calculateMissedDays(user);
            if (missedDays < user.getAlertDays()) {
                continue;
            }

            Optional<CheckIn> lastCheckIn = checkInService.getLastCheckIn(user.getId());
            LocalDateTime lastCheckInTime = lastCheckIn.map(CheckIn::getCheckInTime).orElse(null);
            LocalDateTime countSince = lastCheckInTime == null ? user.getCreatedAt() : lastCheckInTime;

            List<EmergencyContact> contacts = contactRepository.findByUserIdAndIsVerifiedTrue(user.getId());
            for (EmergencyContact contact : contacts) {
                if (!shouldSendAlert(user, contact, countSince)) {
                    continue;
                }

                try {
                    emailService.sendAlertEmail(contact, user, (int) missedDays, lastCheckInTime);
                    notificationService.saveNotification(user, contact, NotificationType.ALERT,
                            NotificationStatus.SENT, null);
                } catch (Exception ex) {
                    notificationService.saveNotification(user, contact, NotificationType.ALERT,
                            NotificationStatus.FAILED, ex.getMessage());
                }
            }
        }
    }

    private boolean shouldSendAlert(User user, EmergencyContact contact, LocalDateTime countSince) {
        LocalDateTime now = TimeUtil.now();

        Optional<com.silemore.entity.Notification> lastAlert =
                notificationService.findLatestAlert(user.getId(), contact.getId());
        if (lastAlert.isPresent() && lastAlert.get().getSentAt() != null) {
            LocalDateTime lastSent = lastAlert.get().getSentAt();
            if (lastSent.isAfter(now.minusHours(24))) {
                return false;
            }
        }

        long alertCount = notificationService.countAlertsSince(user.getId(), contact.getId(), countSince);
        return alertCount < MAX_ALERTS;
    }
}
