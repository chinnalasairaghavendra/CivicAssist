package com.civicassist.notification.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name="notifications",
        indexes = {
                @Index(
                        name="idx_notifications_recipient_created",
                        columnList="recipient_id,created_at"
                ),
                @Index(
                        name="idx_notifications_complaint",
                        columnList = "complaint_id"
                )
        }
)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="recipient_id",nullable=false)
    private Long recipientId;

    @Column(name="complaint_id")
    private Long complaintId;

    @Column(nullable=false,length=150)
    private String title;

    @Column(nullable=false,length=1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length=1000)
    private NotificationType type;

    @Column(name="is_read",nullable=false)
    private boolean read=false;

    @Column(
            name="created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name="read_at")
    private Instant readAt;
    public Notification(){

    }

    public Long getId() {
        return id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
