package com.civicassist.resolution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintResolutionRepository extends JpaRepository<ComplaintResolution,Long> {
    List<ComplaintResolution> findAllByComplaint_IdOrderByResolvedAtDesc(Long complaintId);

    Optional<ComplaintResolution> findByIdAndComplaint_Id(
            Long resolutionId,
            Long complaintId
    );
}
