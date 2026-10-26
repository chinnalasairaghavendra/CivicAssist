package com.civicassist.resolution.dto;

import com.civicassist.resolution.ComplaintResolution;

import java.time.OffsetDateTime;

public record ResolutionResponse(
        Long id,
        Long complaintId,
        String resolutionNotes,
        String evidenceUrl,
        Long resolvedById,
        String resolvedByName,
        OffsetDateTime resolvedAt
) {
    public static ResolutionResponse from(ComplaintResolution resolution){
        return new ResolutionResponse(
                resolution.getId(),
                resolution.getComplaint().getId(),
                resolution.getResolutionNotes(),
                resolution.getEvidenceUrl(),
                resolution.getResolvedBy()!=null?resolution.getResolvedBy().getId():null,
                resolution.getResolvedBy()!=null?resolution.getResolvedBy().getName():null,
                resolution.getResolvedAt()
        );
    }
}
