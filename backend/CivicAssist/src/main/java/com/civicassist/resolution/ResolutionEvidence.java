package com.civicassist.resolution;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "resolution_evidence")
public class ResolutionEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resolution_id", nullable = false)
    private ComplaintResolution resolution;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, unique = true, length = 255)
    private String storedFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = OffsetDateTime.now();
        }
    }

    public ResolutionEvidence() {
    }

    public Long getId() {
        return id;
    }

    public ComplaintResolution getResolution() {
        return resolution;
    }

    public void setResolution(ComplaintResolution resolution) {
        this.resolution = resolution;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }
}