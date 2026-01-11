package com.silemore.service;

import com.silemore.entity.EmergencyContact;
import com.silemore.entity.Notification;
import com.silemore.entity.NotificationStatus;
import com.silemore.entity.NotificationType;
import com.silemore.entity.User;
import com.silemore.repository.NotificationRepository;
import com.silemore.util.TimeUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification saveNotification(User user, EmergencyContact contact, NotificationType type,
                                         NotificationStatus status, String errorMessage) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setContact(contact);
        notification.setType(type);
        notification.setStatus(status);
        if (status == NotificationStatus.SENT || status == NotificationStatus.FAILED) {
            notification.setSentAt(TimeUtil.now());
        }
        notification.setErrorMessage(errorMessage);
        return notificationRepository.save(notification);
    }

    public Optional<Notification> findLatestAlert(Long userId, Long contactId) {
        return notificationRepository.findTopByUserIdAndContactIdAndTypeOrderBySentAtDesc(
                userId, contactId, NotificationType.ALERT);
    }

    public long countAlertsSince(Long userId, Long contactId, LocalDateTime since) {
        return notificationRepository.countByUserIdAndContactIdAndTypeAndSentAtAfter(
                userId, contactId, NotificationType.ALERT, since);
    }
}
