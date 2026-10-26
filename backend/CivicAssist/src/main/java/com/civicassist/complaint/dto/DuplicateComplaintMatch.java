package com.civicassist.complaint.dto;

import com.civicassist.complaint.ComplaintStatus;

public record DuplicateComplaintMatch(
        Long complaintId,
        String title,
        String category,
        ComplaintStatus status,
        int similarityScore
) {
}
