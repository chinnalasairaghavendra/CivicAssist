package com.civicassist.complaint.dto;

import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintStatus;

import java.time.OffsetDateTime;

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
        OffsetDateTime updatedAt
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
                c.getUpdatedAt()
        );
    }
}
