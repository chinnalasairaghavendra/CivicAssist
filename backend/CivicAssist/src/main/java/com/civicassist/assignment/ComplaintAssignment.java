package com.civicassist.assignment;

import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.user.AppUser;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="complaint_assignments")
public class ComplaintAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY,optional = true)
    @JoinColumn(name="complaint_id",nullable=false)
    private Complaint complaint;

    @Column(nullable = false, length = 100)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private AppUser assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name="assignment_note",columnDefinition="TEXT")
    private String assignmentNote;

    @Column(name="is_current",nullable=false)
    private boolean current=true;

    @PrePersist
    protected void onCreate(){
        if(assignedAt==null)
            assignedAt=OffsetDateTime.now();
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department)
    {
        this.department=department;
    }

    public AppUser getAssignedBy() {
        return assignedBy;
    }
    public void setAssignedBy(AppUser assignedBy){
        this.assignedBy=assignedBy;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public String getAssignmentNote() {
        return assignmentNote;
    }

    public void setAssignmentNote(String assignmentNote) {
        this.assignmentNote = assignmentNote;
    }
    public boolean isCurrent(){
        return current;
    }
    public void setCurrent(boolean current){
        this.current=current;
    }
}

