package com.civicassist.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintCommentRepository
        extends JpaRepository<ComplaintComment, Long> {

    List<ComplaintComment> findAllByComplaint_IdOrderByCreatedAtAsc(
            Long complaintId
    );
}