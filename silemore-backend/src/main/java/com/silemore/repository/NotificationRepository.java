package com.silemore.repository;

import com.silemore.entity.Notification;
import com.silemore.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findTopByUserIdAndContactIdAndTypeOrderBySentAtDesc(Long userId,
                                                                              Long contactId,
                                                                              NotificationType type);

    long countByUserIdAndContactIdAndTypeAndSentAtAfter(Long userId,
                                                       Long contactId,
                                                       NotificationType type,
                                                       LocalDateTime sentAt);
}
