package com.civicassist.notification.dto;

import com.civicassist.notification.entity.Notification;
import com.civicassist.notification.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long complaintId,
        String title,
        String message,
        NotificationType type,
        boolean read,
        Instant createdAt,
        Instant readAt
) {
    public static NotificationResponse from(Notification notification){
        return new NotificationResponse(
                notification.getId(),
                notification.getComplaintId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
