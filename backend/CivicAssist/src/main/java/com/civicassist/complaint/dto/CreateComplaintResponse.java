package com.civicassist.complaint.dto;

import java.util.List;

public record CreateComplaintResponse(
        ComplaintResponse complaint,
        boolean duplicateDetected,
        List<DuplicateComplaintMatch> potentialDuplicates
) {
}
