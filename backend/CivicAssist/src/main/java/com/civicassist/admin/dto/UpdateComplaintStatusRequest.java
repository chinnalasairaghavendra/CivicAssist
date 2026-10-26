package com.civicassist.admin.dto;

import com.civicassist.complaint.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateComplaintStatusRequest(
        @NotNull(message="Complaint Status is required")
        ComplaintStatus status,
        @Size(max=1000,message="Reason must notexceed 1000 characters")
        String reason
) {
}
