package com.civicassist.notification.service;


import com.civicassist.notification.entity.Notification;
import com.civicassist.notification.entity.NotificationType;
import com.civicassist.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository)
    {
        this.notificationRepository=notificationRepository;
    }

    @Transactional
    public Notification createNotification(
            Long recipientId,
            Long complaintId,
            String title,
            String message,
            NotificationType type
    ){
        Notification notification=new Notification();
        notification.setRecipientId(recipientId);
        notification.setComplaintId(complaintId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(
            Long recipientId,
            Pageable pageable
    ){
        return notificationRepository
                .findAllByRecipientIdOrderByCreatedAtDesc(
                        recipientId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId){
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long recipientId)
    {
        Notification notification=notificationRepository.findByIdAndRecipientId(notificationId,recipientId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));
        if(notification.isRead()){
            return;
        }
        int updated=notificationRepository.markAsRead(notificationId,recipientId, Instant.now());
        if(updated==0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found");
    }

    @Transactional
    public int markAllAsRead(Long recipientId){
        return notificationRepository.markAllAsRead(recipientId,Instant.now());
    }

    @Transactional
    public void deleteNotification(Long notificationId,Long recipientId){
        long deleted=notificationRepository.deleteByIdAndRecipientId(notificationId,recipientId);
        if(deleted==0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found");
    }

    @Transactional
    public long deleteReadNotifications(Long recipientId){
        return notificationRepository.deleteByRecipientIdAndReadTrue(recipientId);
    }

}
