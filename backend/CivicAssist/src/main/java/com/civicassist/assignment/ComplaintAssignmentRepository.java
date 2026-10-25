package com.civicassist.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment,Long> {
    List<ComplaintAssignment> findAllByComplaint_IdOrderByAssignedAtDesc(Long complaintId);
    Optional<ComplaintAssignment> findByComplaint_IdAndCurrentTrue(Long complaintId);
}
