package com.civicassist.resolution.dto;

import com.civicassist.resolution.ResolutionEvidence;

import java.time.OffsetDateTime;

public record ResolutionEvidenceResponse(
        Long id,
        Long resolutionId,
        String originalFilename,
        String contentType,
        Long fileSize,
        OffsetDateTime uploadedAt
) {
    public static ResolutionEvidenceResponse from(
            ResolutionEvidence evidence
    ) {
        return new ResolutionEvidenceResponse(
                evidence.getId(),
                evidence.getResolution().getId(),
                evidence.getOriginalFilename(),
                evidence.getContentType(),
                evidence.getFileSize(),
                evidence.getUploadedAt()
        );
    }
}