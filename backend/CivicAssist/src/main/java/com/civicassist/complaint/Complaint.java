package com.civicassist.complaint;

import com.civicassist.user.AppUser;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length=160)
    private String title;

    @Column(nullable=false,columnDefinition = "TEXT")
    private String description;

    @Column(length=80)
    private String category;

    @Column(length=255)
    private String address;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length=30)
    private ComplaintStatus status=ComplaintStatus.SUBMITTED;

    @Column(name="created_at",nullable=false,updatable = false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at",nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "ai_category", length = 80)
    private String aiCategory;

    @Column(name = "ai_urgency", length = 20)
    private String aiUrgency;

    @Column(name = "recommended_department", length = 100)
    private String recommendedDepartment;

    @Column(name = "ai_reasoning", columnDefinition = "TEXT")
    private String aiReasoning;

    @Column(name = "triaged_at")
    private OffsetDateTime triagedAt;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id")
    private AppUser user;


    protected Complaint() {
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getAiCategory() {
        return aiCategory;
    }

    public void setAiCategory(String aiCategory) {
        this.aiCategory = aiCategory;
    }

    public String getAiUrgency() {
        return aiUrgency;
    }

    public void setAiUrgency(String aiUrgency) {
        this.aiUrgency = aiUrgency;
    }

    public String getRecommendedDepartment() {
        return recommendedDepartment;
    }

    public void setRecommendedDepartment(String recommendedDepartment) {
        this.recommendedDepartment = recommendedDepartment;
    }

    public String getAiReasoning() {
        return aiReasoning;
    }

    public void setAiReasoning(String aiReasoning) {
        this.aiReasoning = aiReasoning;
    }

    public OffsetDateTime getTriagedAt() {
        return triagedAt;
    }

    public void setTriagedAt(OffsetDateTime triagedAt) {
        this.triagedAt = triagedAt;
    }
}
