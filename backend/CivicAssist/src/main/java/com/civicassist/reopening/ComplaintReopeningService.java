package com.civicassist.reopening;

import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.history.ComplaintStatusHistory;
import com.civicassist.history.ComplaintStatusHistoryRepository;
import com.civicassist.reopening.dto.CreateReopeningRequest;
import com.civicassist.reopening.dto.ReopeningRequestResponse;
import com.civicassist.reopening.dto.ReviewReopeningRequest;
import com.civicassist.user.AppUser;
import com.civicassist.user.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ComplaintReopeningService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintReopeningRequestRepository reopeningRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public ComplaintReopeningService(
            ComplaintRepository complaintRepository,
            ComplaintReopeningRequestRepository reopeningRepository,
            ComplaintStatusHistoryRepository historyRepository,
            UserRepository userRepository
    ) {
        this.complaintRepository = complaintRepository;
        this.reopeningRepository = reopeningRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReopeningRequestResponse createRequest(
            Long complaintId,
            CreateReopeningRequest request,
            String authenticatedEmail
    ) {
        Complaint complaint = complaintRepository
                .findByIdAndUser_Email(complaintId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only resolved complaints can be reopened"
            );
        }

        if (reopeningRepository.existsByComplaint_IdAndStatus(
                complaintId,
                ReopeningRequestStatus.PENDING
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A reopening request is already pending"
            );
        }

        AppUser citizen = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User account not found"
                ));

        ComplaintReopeningRequest reopeningRequest =
                new ComplaintReopeningRequest();

        reopeningRequest.setComplaint(complaint);
        reopeningRequest.setRequestedBy(citizen);
        reopeningRequest.setReason(request.reason().trim());
        reopeningRequest.setStatus(ReopeningRequestStatus.PENDING);

        try {
            reopeningRequest = reopeningRepository.saveAndFlush(
                    reopeningRequest
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A reopening request is already pending"
            );
        }

        return ReopeningRequestResponse.from(reopeningRequest);
    }

    @Transactional(readOnly = true)
    public List<ReopeningRequestResponse> getCitizenRequests(
            Long complaintId,
            String authenticatedEmail
    ) {
        Complaint complaint = complaintRepository
                .findByIdAndUser_Email(complaintId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        return reopeningRepository
                .findAllByComplaint_IdOrderByCreatedAtDesc(complaint.getId())
                .stream()
                .map(ReopeningRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReopeningRequestResponse> getPendingRequests() {
        return reopeningRepository
                .findAllByStatusOrderByCreatedAtAsc(
                        ReopeningRequestStatus.PENDING
                )
                .stream()
                .map(ReopeningRequestResponse::from)
                .toList();
    }

    @Transactional
    public ReopeningRequestResponse reviewRequest(
            Long requestId,
            ReviewReopeningRequest request,
            String authenticatedEmail
    ) {
        ComplaintReopeningRequest reopeningRequest =
                reopeningRepository.findById(requestId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Reopening request not found"
                        ));

        if (reopeningRequest.getStatus()
                != ReopeningRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This reopening request has already been reviewed"
            );
        }

        if (request.decision() == ReopeningRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Decision must be APPROVED or REJECTED"
            );
        }

        AppUser admin = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Administrator account not found"
                ));

        Complaint complaint = reopeningRequest.getComplaint();

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The complaint is no longer resolved"
            );
        }

        if (request.decision() == ReopeningRequestStatus.APPROVED) {
            ComplaintStatus previousStatus = complaint.getStatus();

            complaint.setStatus(ComplaintStatus.UNDER_REVIEW);
            complaintRepository.save(complaint);

            ComplaintStatusHistory history =
                    new ComplaintStatusHistory();

            history.setComplaint(complaint);
            history.setPreviousStatus(previousStatus);
            history.setNewStatus(ComplaintStatus.UNDER_REVIEW);
            history.setChangedBy(admin);
            history.setChangeReason(
                    "Complaint reopened after citizen request: "
                            + reopeningRequest.getReason()
            );

            historyRepository.save(history);
        }

        reopeningRequest.setStatus(request.decision());
        reopeningRequest.setReviewedBy(admin);
        reopeningRequest.setReviewedAt(OffsetDateTime.now());
        reopeningRequest.setAdminComment(
                request.adminComment() == null
                        || request.adminComment().isBlank()
                        ? null
                        : request.adminComment().trim()
        );

        return ReopeningRequestResponse.from(
                reopeningRepository.save(reopeningRequest)
        );
    }
}