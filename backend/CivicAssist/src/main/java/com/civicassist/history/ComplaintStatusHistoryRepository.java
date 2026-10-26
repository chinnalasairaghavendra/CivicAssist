package com.civicassist.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory,Long> {
    List<ComplaintStatusHistory> findAllByComplaint_IdOrderByChangedAtDesc(Long complaintId);
}
