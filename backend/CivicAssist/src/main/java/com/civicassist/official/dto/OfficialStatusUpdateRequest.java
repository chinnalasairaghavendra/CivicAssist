package com.civicassist.official.dto;

import com.civicassist.complaint.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OfficialStatusUpdateRequest(
        @NotNull
        ComplaintStatus status,

        @Size(max = 1000)
        String changeReason
) {}