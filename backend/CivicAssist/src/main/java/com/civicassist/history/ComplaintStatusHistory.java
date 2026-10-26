package com.civicassist.history;

import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.user.AppUser;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="complaint_status_history")
public class ComplaintStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="complaint_id",nullable=false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name="previous_status",length=30)
    private ComplaintStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="new_status",nullable=false,length=30)
    private ComplaintStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="changed_by")
    private AppUser changedBy;

    @Column(name="change_reason",length=1000)
    private String changeReason;

    @Column(name="changed_at",nullable=false)
    private OffsetDateTime changedAt;

    @PrePersist
    protected void onCreate(){
        if(changedAt==null){
            changedAt=OffsetDateTime.now();
        }
    }

    public Long getId(){
        return id;
    }

    public Complaint getComplaint(){
        return complaint;
    }

    public void setComplaint(Complaint complaint){
        this.complaint=complaint;
    }

    public ComplaintStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ComplaintStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ComplaintStatus newStatus) {
        this.newStatus = newStatus;
    }

    public AppUser getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(AppUser changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

}
