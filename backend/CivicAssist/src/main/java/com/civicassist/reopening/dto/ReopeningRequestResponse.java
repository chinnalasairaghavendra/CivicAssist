package com.civicassist.reopening.dto;

import com.civicassist.reopening.ComplaintReopeningRequest;
import com.civicassist.reopening.ReopeningRequestStatus;

import java.time.OffsetDateTime;

public record ReopeningRequestResponse(
        Long id,
        Long complaintId,
        Long requestedById,
        String requestedByName,
        String reason,
        ReopeningRequestStatus status,
        Long reviewedById,
        String reviewedByName,
        String adminComment,
        OffsetDateTime createdAt,
        OffsetDateTime reviewedAt
) {
    public static ReopeningRequestResponse from(
            ComplaintReopeningRequest request
    ) {
        return new ReopeningRequestResponse(
                request.getId(),
                request.getComplaint().getId(),
                request.getRequestedBy().getId(),
                request.getRequestedBy().getName(),
                request.getReason(),
                request.getStatus(),
                request.getReviewedBy() == null
                        ? null
                        : request.getReviewedBy().getId(),
                request.getReviewedBy() == null
                        ? null
                        : request.getReviewedBy().getName(),
                request.getAdminComment(),
                request.getCreatedAt(),
                request.getReviewedAt()
        );
    }
}