package com.civicassist.assignment;

import com.civicassist.complaint.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment,Long> {
    List<ComplaintAssignment> findAllByComplaint_IdOrderByAssignedAtDesc(Long complaintId);
    Optional<ComplaintAssignment> findByComplaint_IdAndCurrentTrue(Long complaintId);
    Page<ComplaintAssignment> findAllByDepartment_IdAndCurrentTrue(
            Long departmentId,
            Pageable pageable
    );

    // Current assignments filtered by complaint status
    Page<ComplaintAssignment>
    findAllByDepartment_IdAndCurrentTrueAndComplaint_Status(
            Long departmentId,
            ComplaintStatus status,
            Pageable pageable
    );

    // Find a specific complaint's current assignment in a department
    Optional<ComplaintAssignment>
    findByComplaint_IdAndDepartment_IdAndCurrentTrue(
            Long complaintId,
            Long departmentId
    );
}
