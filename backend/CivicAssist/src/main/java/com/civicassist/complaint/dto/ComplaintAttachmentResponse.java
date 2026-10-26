package com.civicassist.complaint.dto;

import com.civicassist.complaint.ComplaintAttachment;

import java.time.OffsetDateTime;

public record ComplaintAttachmentResponse(
        Long id,
        Long complaintId,
        String originalFilename,
        String contentType,
        long fileSize,
        OffsetDateTime uploadedAt
) {
    public static ComplaintAttachmentResponse from(
            ComplaintAttachment attachment
    ) {
        return new ComplaintAttachmentResponse(
                attachment.getId(),
                attachment.getComplaint().getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedAt()
        );
    }
}