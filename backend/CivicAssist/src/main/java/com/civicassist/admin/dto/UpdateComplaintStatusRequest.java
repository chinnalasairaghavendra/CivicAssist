package com.civicassist.admin.dto;

import com.civicassist.complaint.ComplaintStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateComplaintStatusRequest(
        @NotNull(message="Complaint Status is required")
        ComplaintStatus status
) {
}
