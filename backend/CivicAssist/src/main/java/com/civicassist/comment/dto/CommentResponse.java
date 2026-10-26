package com.civicassist.comment.dto;

import java.time.OffsetDateTime;

public record CommentResponse(
        Long id,
        Long complaintId,
        Long authorId,
        String authorName,
        String content,
        OffsetDateTime createdAt
) {
}
