package com.civicassist.assignment.dto;

import com.civicassist.assignment.ComplaintAssignment;

import java.time.OffsetDateTime;

public record AssignmentResponse(
        Long id,
        Long complaintId,
        String department,
        Long assignedById,
        String assignedByName,
        OffsetDateTime assignedAt,
        String assignmentNote,
        boolean current
) {

    public static AssignmentResponse from(ComplaintAssignment assignment){
        return new AssignmentResponse(assignment.getId(),
                assignment.getComplaint().getId(),
                assignment.getDepartment(),
                assignment.getAssignedBy()!=null
                ?assignment.getAssignedBy().getId():null,
                assignment.getAssignedBy()!=null?assignment.getAssignedBy().getName():null,
                assignment.getAssignedAt(),
                assignment.getAssignmentNote(),
                assignment.isCurrent()
                );
    }
}
