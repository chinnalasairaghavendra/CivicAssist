package com.civicassist.resolution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintResolutionRepository extends JpaRepository<ComplaintResolution,Long> {
    List<ComplaintResolution> findAllByComplaint_IdOrderByResolvedAtDesc(Long complaintId);

}
