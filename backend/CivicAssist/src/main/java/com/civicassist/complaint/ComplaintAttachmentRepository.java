package com.civicassist.complaint;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment,Long> {
    List<ComplaintAttachment> findAllByComplaint_IdOrderByUploadedAtDesc(
            Long complaintId
    );

    Optional<ComplaintAttachment> findByIdAndComplaint_Id(
            Long id,
            Long complaintId
    );
    long countByComplaint_Id(Long complaintId);
}
