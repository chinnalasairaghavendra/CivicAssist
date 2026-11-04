package com.civicassist.notification.service;


import com.civicassist.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationCleanupService {
    private static final Logger log= LoggerFactory.getLogger(NotificationCleanupService.class);
    private final NotificationRepository notificationRepository;

    @Value("${app.notifications.retention.read-days:30}")
    private int readRetentionDays;

    @Value("${app.notifications.retention.unread-days:90}")
    private int unreadRetentionDays;

    @Value("${app.notifications.cleanup.batch-size:1000}")
    private int batchSize;

    public NotificationCleanupService(
            NotificationRepository notificationRepository
    ){
        this.notificationRepository=notificationRepository;
    }

    @Scheduled(cron = "${app.notifications.cleanup.cron:0 0 2 * * *}")
    @Transactional
    public void cleanupExpiredNotifications(){
        if(readRetentionDays<1||unreadRetentionDays<1||batchSize<1){
            log.error("Invalid notification cleanup configuration");
            return;
        }
        Instant now=Instant.now();
        Instant readCutoff=now.minus(readRetentionDays, ChronoUnit.DAYS);
        Instant unreadCutoff=now.minus(unreadRetentionDays,ChronoUnit.DAYS);
        int totalDeleted=0;
        int deleted;

        do{
            deleted=notificationRepository.deleteExpiredNotifications(
                    readCutoff,
                    unreadCutoff,
                    batchSize
            );
            totalDeleted+=deleted;
        }
        while(deleted==batchSize);
        log.info(
                "Notification cleanup completed.Deleted {} notifications.",
                totalDeleted
        );
    }

}
