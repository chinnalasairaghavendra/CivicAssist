package com.civicassist.resolution;

import com.civicassist.complaint.Complaint;
import com.civicassist.user.AppUser;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="complaint_resolutions")
public class ComplaintResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY,optional = false)
    @JoinColumn(name="complaint_id",nullable=false)
    private Complaint complaint;

    @Column(name="resolution_notes",nullable=false,columnDefinition="TEXT")
    private String resolutionNotes;

    @Column(name="evidence_url",length=2048)
    private String evidenceUrl;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="resolved_by")
    private AppUser resolvedBy;

    @Column(name="resolved_at",nullable = false)
    private OffsetDateTime resolvedAt;

    @PrePersist
    protected void onCreate(){
        if(resolvedAt==null)
            resolvedAt=OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public AppUser getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(AppUser resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

}

