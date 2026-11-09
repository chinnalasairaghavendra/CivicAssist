package com.civicassist.complaint.dto;

import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.triage.dto.ExtractedDetail;

import java.time.OffsetDateTime;
import java.util.List;

public record ComplaintResponse(
        Long id,
        String title,
        String description,
        String category,
        String address,
        Double latitude,
        Double longitude,
        ComplaintStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String aiCategory,
        String aiUrgency,
        String recommendedDepartment,
        String aiReasoning,
        OffsetDateTime triagedAt,
        String aiPromptVersion,
        String aiModel,
        String aiSummary,
        List<ExtractedDetail> aiExtractedDetails,
        List<String> aiMissingInformation
) {
    public static ComplaintResponse from(Complaint c) {
        return new ComplaintResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getCategory(),
                c.getAddress(),
                c.getLatitude(),
                c.getLongitude(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getAiCategory(),
                c.getAiUrgency(),
                c.getRecommendedDepartment(),
                c.getAiReasoning(),
                c.getTriagedAt(),
                c.getAiPromptVersion(),
                c.getAiModel(),
                c.getAiSummary(),
                c.getAiExtractedDetails(),
                c.getAiMissingInformation()
        );
    }
}
