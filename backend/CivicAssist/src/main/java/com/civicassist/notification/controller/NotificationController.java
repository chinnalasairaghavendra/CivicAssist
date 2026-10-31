package com.civicassist.notification.controller;

import com.civicassist.notification.dto.NotificationResponse;
import com.civicassist.notification.entity.Notification;
import com.civicassist.notification.service.NotificationService;
import com.civicassist.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository)
    {
        this.notificationService=notificationService;
        this.userRepository=userRepository;
    }

    private Long getAuthenticatedUserId(Jwt jwt){
        String email=jwt.getSubject();
        return userRepository.findByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found")).getId();
    }

    @GetMapping
    public Page<NotificationResponse> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size=20)Pageable pageable
            ){
        Long userId=getAuthenticatedUserId(jwt);
        Page<Notification> notifications=notificationService.getNotifications(userId,pageable);
        return notifications.map(NotificationResponse::from);
    }

    @GetMapping("/unread-count")
    public Map<String,Long> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId=getAuthenticatedUserId(jwt);
        return Map.of("unreadCount",
                notificationService.getUnreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    public Map<String,String> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ){
        Long userId=getAuthenticatedUserId(jwt);
        notificationService.markAsRead(id,userId);
        return Map.of(
                "message",
                "Notification marked as read"
        );

    }

    @PostMapping("/read-all")
    public Map<String,Integer> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId=getAuthenticatedUserId(jwt);
        int updated=notificationService.markAllAsRead(userId);
        return Map.of("updatedCount",updated);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ){
        Long userId=getAuthenticatedUserId(jwt);
        notificationService.deleteNotification(id,userId);
    }

    @DeleteMapping("/read")
    public Map<String,Long> deleteReadNotifications(
            @AuthenticationPrincipal Jwt jwt
    ){
        Long userId=getAuthenticatedUserId(jwt);
        return Map.of("deletedCount",notificationService.deleteReadNotifications(userId));
    }
}

