package com.civicassist.history.dto;

import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.history.ComplaintStatusHistory;

import java.time.OffsetDateTime;

public record ComplaintStatusHistoryResponse(
        Long id,
        Long complaintId,
        ComplaintStatus previousStatus,
        ComplaintStatus newStatus,
        Long changedById,
        String changedByName,
        String changeReason,
        OffsetDateTime changedAt
) {
    public static ComplaintStatusHistoryResponse from(ComplaintStatusHistory history){
        return new ComplaintStatusHistoryResponse(
                history.getId(),
                history.getComplaint().getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedBy()!=null?history.getChangedBy().getId():null,
                history.getChangedBy()!=null?history.getChangedBy().getName():null,
                history.getChangeReason(),
                history.getChangedAt()
        );
    }
}
