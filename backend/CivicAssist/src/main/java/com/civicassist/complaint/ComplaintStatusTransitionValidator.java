package com.civicassist.complaint;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class ComplaintStatusTransitionValidator {

    private static final Map<ComplaintStatus, Set<ComplaintStatus>>
            ADMIN_TRANSITIONS = new EnumMap<>(ComplaintStatus.class);

    private static final Map<ComplaintStatus, Set<ComplaintStatus>>
            OFFICIAL_TRANSITIONS = new EnumMap<>(ComplaintStatus.class);

    static {
        // Admin transitions
        ADMIN_TRANSITIONS.put(
                ComplaintStatus.SUBMITTED,
                EnumSet.of(ComplaintStatus.UNDER_REVIEW)
        );

        ADMIN_TRANSITIONS.put(
                ComplaintStatus.UNDER_REVIEW,
                EnumSet.of(
                        ComplaintStatus.ASSIGNED,
                        ComplaintStatus.REJECTED
                )
        );

        ADMIN_TRANSITIONS.put(
                ComplaintStatus.ASSIGNED,
                EnumSet.of(
                        ComplaintStatus.UNDER_REVIEW,
                        ComplaintStatus.REJECTED
                )
        );

        ADMIN_TRANSITIONS.put(
                ComplaintStatus.IN_PROGRESS,
                EnumSet.of(ComplaintStatus.REJECTED)
        );

        // Official transitions
        OFFICIAL_TRANSITIONS.put(
                ComplaintStatus.ASSIGNED,
                EnumSet.of(ComplaintStatus.IN_PROGRESS)
        );

        // Resolution and reopening are handled by dedicated workflows.
    }

    public void validateAdminTransition(
            ComplaintStatus currentStatus,
            ComplaintStatus newStatus
    ) {
        validate(currentStatus, newStatus, ADMIN_TRANSITIONS);
    }

    public void validateOfficialTransition(
            ComplaintStatus currentStatus,
            ComplaintStatus newStatus
    ) {
        validate(currentStatus, newStatus, OFFICIAL_TRANSITIONS);
    }

    private void validate(
            ComplaintStatus currentStatus,
            ComplaintStatus newStatus,
            Map<ComplaintStatus, Set<ComplaintStatus>> transitions
    ) {
        if (currentStatus == null || newStatus == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current status and new status must not be null"
            );
        }

        if (currentStatus == newStatus) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Complaint already has this status"
            );
        }

        Set<ComplaintStatus> allowedStatuses =
                transitions.get(currentStatus);

        if (allowedStatuses == null
                || !allowedStatuses.contains(newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition from "
                            + currentStatus + " to " + newStatus
            );
        }
    }
}