package com.civicassist.official;

import com.civicassist.assignment.ComplaintAssignment;
import com.civicassist.assignment.ComplaintAssignmentRepository;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.complaint.ComplaintStatusTransitionValidator;
import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.history.ComplaintStatusHistory;
import com.civicassist.history.ComplaintStatusHistoryRepository;
import com.civicassist.user.AppUser;
import com.civicassist.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OfficialComplaintService {

    private final ComplaintAssignmentRepository assignmentRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final ComplaintStatusTransitionValidator transitionValidator;

    public OfficialComplaintService(
            ComplaintAssignmentRepository assignmentRepository,
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            ComplaintStatusHistoryRepository historyRepository,
            ComplaintStatusTransitionValidator transitionValidator
    ) {
        this.assignmentRepository = assignmentRepository;
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.transitionValidator=transitionValidator;
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getDepartmentComplaints(
            String email,
            ComplaintStatus status,
            int page,
            int size
    ) {
        AppUser official = getOfficial(email);
        Long departmentId = getDepartmentId(official);

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "assignedAt")
        );

        Page<ComplaintAssignment> assignments;

        if (status == null) {
            assignments =
                    assignmentRepository
                            .findAllByDepartment_IdAndCurrentTrue(
                                    departmentId,
                                    pageable
                            );
        } else {
            assignments =
                    assignmentRepository
                            .findAllByDepartment_IdAndCurrentTrueAndComplaint_Status(
                                    departmentId,
                                    status,
                                    pageable
                            );
        }

        return assignments.map(assignment ->
                ComplaintResponse.from(assignment.getComplaint())
        );
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaint(
            Long complaintId,
            String email
    ) {
        AppUser official = getOfficial(email);
        Long departmentId = getDepartmentId(official);

        ComplaintAssignment assignment =
                getCurrentAssignment(complaintId, departmentId);

        return ComplaintResponse.from(assignment.getComplaint());
    }

    @Transactional
    public ComplaintResponse updateStatus(
            Long complaintId,
            String email,
            ComplaintStatus newStatus,
            String changeReason
    ) {
        AppUser official = getOfficial(email);
        Long departmentId = getDepartmentId(official);

        ComplaintAssignment assignment =
                getCurrentAssignment(complaintId, departmentId);

        Complaint complaint = assignment.getComplaint();

        ComplaintStatus previousStatus = complaint.getStatus();

        transitionValidator.validateOfficialTransition(
                previousStatus,
                newStatus
        );

        complaint.setStatus(newStatus);

        Complaint savedComplaint = complaintRepository.save(complaint);

        ComplaintStatusHistory history =
                new ComplaintStatusHistory();

        history.setComplaint(savedComplaint);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(official);
        history.setChangeReason(
                changeReason == null || changeReason.isBlank()
                        ? null
                        : changeReason.trim()
        );

        historyRepository.save(history);

        return ComplaintResponse.from(savedComplaint);
    }

    private AppUser getOfficial(String email) {
        AppUser official = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        if (official.getRole() == null
                || !official.getRole().name().equals("OFFICIAL")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only department officials can access this resource"
            );
        }

        return official;
    }

    private Long getDepartmentId(AppUser official) {
        if (official.getDepartment() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No department is assigned to this official"
            );
        }

        return official.getDepartment().getId();
    }

    private ComplaintAssignment getCurrentAssignment(
            Long complaintId,
            Long departmentId
    ) {
        return assignmentRepository
                .findByComplaint_IdAndDepartment_IdAndCurrentTrue(
                        complaintId,
                        departmentId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found in your department's queue"
                ));
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100"
            );
        }
    }
}