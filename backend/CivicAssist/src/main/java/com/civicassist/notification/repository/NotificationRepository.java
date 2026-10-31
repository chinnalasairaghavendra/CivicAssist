package com.civicassist.notification.repository;

import com.civicassist.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Page<Notification> findAllByRecipientIdOrderByCreatedAtDesc(
            Long recipientId,
            Pageable pageable
    );

    Optional<Notification> findByIdAndRecipientId(
            Long id,
            Long recipientId
    );

    long countByRecipientIdAndReadFalse(Long recipientId);

    long deleteByIdAndRecipientId(Long id,Long recipientId);

    long deleteByRecipientIdAndReadTrue(
            Long recipientId
    );

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.read =true,
        n.readAt=:readAt
        WHERE n.id = :notificationId
        AND n.recipientId = :recipientId
        AND n.read=false
""")
    int markAsRead(
            @Param("notificationId") Long notificationId,
            @Param("recipientId") Long recipientId,
            @Param("readAt") Instant readAt
    );

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.read = true,
        n.readAt=:readAt
        WHERE n.recipientId = :recipientId
        AND n.read=false
""")
    int markAllAsRead(
            @Param("recipientId") Long recipientId,
            @Param("readAt") Instant readAt
    );

    @Modifying
    @Query(value= """
        DELETE FROM notifications
        WHERE id IN(
         SELECT id 
         FROM notifications
         WHERE(is_read = TRUE AND read_at < :readCutoff)
         OR
         (is_read = FALSE AND created_at < :unreadCutoff)
         ORDER BY created_at
         LIMIT :batchSize
         )
""",nativeQuery = true)
    int deleteExpiredNotifications(
            @Param("readCutoff") Instant readCutoff,
            @Param("unreadCutoff") Instant unreadCutoff,
            @Param("batchSize") int batchSize
    );
}
