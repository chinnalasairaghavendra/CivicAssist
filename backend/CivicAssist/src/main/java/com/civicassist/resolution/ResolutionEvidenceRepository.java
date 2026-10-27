package com.civicassist.resolution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResolutionEvidenceRepository
        extends JpaRepository<ResolutionEvidence, Long> {

    List<ResolutionEvidence> findAllByResolution_IdOrderByUploadedAtDesc(
            Long resolutionId
    );

    Optional<ResolutionEvidence> findByIdAndResolution_Id(
            Long id,
            Long resolutionId
    );

    long countByResolution_Id(Long resolutionId);
}