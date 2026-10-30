package com.civicassist.admin;

import com.civicassist.admin.dto.UpdateComplaintStatusRequest;
import com.civicassist.assignment.ComplaintAssignmentRepository;
import com.civicassist.complaint.*;
import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.history.ComplaintStatusHistory;
import com.civicassist.history.ComplaintStatusHistoryRepository;
import com.civicassist.history.dto.ComplaintStatusHistoryResponse;
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

import java.util.List;

@Service
public class AdminComplaintService {
    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
    private final UserRepository userRepository;
    private final ComplaintStatusTransitionValidator transitionValidator;
    private final ComplaintAssignmentRepository assignmentRepository;

    public AdminComplaintService(ComplaintRepository complaintRepository,
                                 ComplaintStatusHistoryRepository complaintStatusHistoryRepository,
                                 UserRepository userRepository,
                                 ComplaintStatusTransitionValidator complaintStatusTransitionValidator,
                                 ComplaintAssignmentRepository assignmentRepository){
        this.complaintRepository=complaintRepository;
        this.complaintStatusHistoryRepository=complaintStatusHistoryRepository;
        this.userRepository=userRepository;
        this.transitionValidator=complaintStatusTransitionValidator;
        this.assignmentRepository=assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> findAllComplaints(){
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ComplaintResponse::from)
                .toList();
    }

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId,
                                          UpdateComplaintStatusRequest request,
                                          String authenticatedEmail){
        Complaint complaint=complaintRepository.findById(complaintId).
                orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Complaint not found"));
        AppUser admin=userRepository.findByEmail(authenticatedEmail)
                        .orElseThrow(()-> new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Administrator account not found"
                        ));
        ComplaintStatus previousStatus = complaint.getStatus();
        ComplaintStatus newStatus = request.status();

        transitionValidator.validateAdminTransition(
                previousStatus,
                newStatus
        );
        if (newStatus == ComplaintStatus.ASSIGNED) {
            boolean hasCurrentAssignment = assignmentRepository
                    .findByComplaint_IdAndCurrentTrue(complaintId)
                    .isPresent();

            if (!hasCurrentAssignment) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A complaint must have a current department assignment "
                                + "before it can be marked ASSIGNED"
                );
            }
        }
        ComplaintStatusHistory history=new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(admin);
        history.setChangeReason(
                request.reason()==null||request.reason().isBlank()
                ?null
                        :request.reason().trim()
        );
        complaint.setStatus(newStatus);
        complaintRepository.save(complaint);
        complaintStatusHistoryRepository.save(history);
        return ComplaintResponse.from(complaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintStatusHistoryResponse> getStatusHistory(
            Long complaintId
    ){
        if(!complaintRepository.existsById(complaintId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND
            ,"Complaint not found");
        }
        return complaintStatusHistoryRepository
                .findAllByComplaint_IdOrderByChangedAtDesc(complaintId)
                .stream()
                .map(ComplaintStatusHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> findComplaints(
            int page,
            int size,
            ComplaintStatus status,
            String category,
            String keyword,
            String sortBy,
            String direction
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        // Only allow explicitly approved sorting fields.
        List<String> allowedSortFields = List.of(
                "createdAt",
                "updatedAt",
                "title",
                "status",
                "category"
        );

        if (sortBy == null || !allowedSortFields.contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort field"
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Direction must be 'asc' or 'desc'"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return complaintRepository.findAll(
                        ComplaintSpecification.filter(
                                status,
                                category,
                                keyword
                        ),
                        pageable
                )
                .map(ComplaintResponse::from);
    }

    private boolean isValidTransition(
            ComplaintStatus current,
            ComplaintStatus next
    ) {
        return switch (current) {
            case SUBMITTED ->
                    next == ComplaintStatus.UNDER_REVIEW
                            || next == ComplaintStatus.REJECTED;

            case UNDER_REVIEW ->
                    next == ComplaintStatus.ASSIGNED
                            || next == ComplaintStatus.IN_PROGRESS
                            || next == ComplaintStatus.REJECTED;

            case ASSIGNED ->
                    next == ComplaintStatus.IN_PROGRESS
                            || next == ComplaintStatus.UNDER_REVIEW
                            || next == ComplaintStatus.REJECTED;

            case IN_PROGRESS ->
                    next == ComplaintStatus.UNDER_REVIEW
                            || next == ComplaintStatus.REJECTED;

            case RESOLVED, REJECTED -> false;
        };
    }
}
