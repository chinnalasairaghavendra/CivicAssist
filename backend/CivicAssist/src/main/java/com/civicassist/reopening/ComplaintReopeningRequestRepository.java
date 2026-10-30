package com.civicassist.reopening;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintReopeningRequestRepository
        extends JpaRepository<ComplaintReopeningRequest, Long> {

    List<ComplaintReopeningRequest>
    findAllByComplaint_IdOrderByCreatedAtDesc(Long complaintId);

    List<ComplaintReopeningRequest>
    findAllByStatusOrderByCreatedAtAsc(
            ReopeningRequestStatus status
    );

    Optional<ComplaintReopeningRequest>
    findByIdAndComplaint_Id(
            Long id,
            Long complaintId
    );

    boolean existsByComplaint_IdAndStatus(
            Long complaintId,
            ReopeningRequestStatus status
    );
}