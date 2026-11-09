package com.civicassist.complaint;

import com.civicassist.triage.dto.ExtractedDetail;
import com.civicassist.user.AppUser;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

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

    @Column(name = "ai_prompt_version", length = 30)
    private String aiPromptVersion;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id")
    private AppUser user;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_extracted_details", columnDefinition = "JSONB")
    private List<ExtractedDetail> aiExtractedDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_missing_information", columnDefinition = "JSONB")
    private List<String> aiMissingInformation;


    @Enumerated(EnumType.STRING)
    @Column(name = "ai_triage_status", length = 20)
    private AiTriageStatus aiTriageStatus = AiTriageStatus.PENDING;

    @Column(name = "ai_triage_attempts", nullable = false)
    private Integer aiTriageAttempts = 0;

    @Column(name = "ai_triage_error", columnDefinition = "TEXT")
    private String aiTriageError;

    @Column(name = "ai_last_attempt_at")
    private OffsetDateTime aiLastAttemptAt;

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

    public AppUser getUser(){
        return user;
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

    public String getAiPromptVersion() {
        return aiPromptVersion;
    }

    public void setAiPromptVersion(String aiPromptVersion) {
        this.aiPromptVersion = aiPromptVersion;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public List<ExtractedDetail> getAiExtractedDetails() {
        return aiExtractedDetails;
    }

    public void setAiExtractedDetails(List<ExtractedDetail> aiExtractedDetails) {
        this.aiExtractedDetails = aiExtractedDetails;
    }

    public List<String> getAiMissingInformation() {
        return aiMissingInformation;
    }

    public void setAiMissingInformation(List<String> aiMissingInformation) {
        this.aiMissingInformation = aiMissingInformation;
    }

    public AiTriageStatus getAiTriageStatus() {
        return aiTriageStatus;
    }

    public void setAiTriageStatus(AiTriageStatus aiTriageStatus) {
        this.aiTriageStatus = aiTriageStatus;
    }

    public Integer getAiTriageAttempts() {
        return aiTriageAttempts;
    }

    public void setAiTriageAttempts(Integer aiTriageAttempts) {
        this.aiTriageAttempts = aiTriageAttempts;
    }

    public String getAiTriageError() {
        return aiTriageError;
    }

    public void setAiTriageError(String aiTriageError) {
        this.aiTriageError = aiTriageError;
    }

    public OffsetDateTime getAiLastAttemptAt() {
        return aiLastAttemptAt;
    }

    public void setAiLastAttemptAt(OffsetDateTime aiLastAttemptAt) {
        this.aiLastAttemptAt = aiLastAttemptAt;
    }
}
