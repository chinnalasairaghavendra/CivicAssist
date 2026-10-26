package com.civicassist.complaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint,Long>, JpaSpecificationExecutor<Complaint> {
    List<Complaint> findAllByOrderByCreatedAtDesc();
    List<Complaint> findAllByUser_EmailOrderByCreatedAtDesc(String email);

    Optional<Complaint> findByIdAndUser_Email(
            Long id,
            String email
    );

    List<Complaint> findAllByStatusNotIn(
            List<ComplaintStatus> statuses
    );
    Page<Complaint> findAllByStatusNotIn(
            List<ComplaintStatus> statuses,
            Pageable pageable
    );
}
