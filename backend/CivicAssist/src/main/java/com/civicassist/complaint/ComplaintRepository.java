package com.civicassist.complaint;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint,Long> {
    List<Complaint> findAllByOrderByCreatedAtDesc();
    List<Complaint> findAllByUser_EmailOrderByCreatedAtDesc(String email);

    Optional<Complaint> findByIdAndUser_Email(
            Long id,
            String email
    );

}
