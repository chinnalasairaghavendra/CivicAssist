package com.civicassist.resolution;


import com.civicassist.assignment.ComplaintAssignmentRepository;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.history.ComplaintStatusHistory;
import com.civicassist.history.ComplaintStatusHistoryRepository;
import com.civicassist.resolution.dto.CreateResolutionRequest;
import com.civicassist.resolution.dto.ResolutionResponse;
import com.civicassist.user.AppUser;
import com.civicassist.user.UserRepository;
import com.civicassist.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ComplaintResolutionService {
    private final ComplaintRepository complaintRepository;
    private final ComplaintResolutionRepository complaintResolutionRepository;
    private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
    private final UserRepository userRepository;
    private final ComplaintAssignmentRepository assignmentRepository;

    public ComplaintResolutionService(
            ComplaintRepository complaintRepository,
            ComplaintResolutionRepository resolutionRepository,
            ComplaintStatusHistoryRepository historyRepository,
            UserRepository userRepository,
            ComplaintAssignmentRepository complaintAssignmentRepository) {

        this.complaintRepository = complaintRepository;
        this.complaintResolutionRepository = resolutionRepository;
        this.complaintStatusHistoryRepository = historyRepository;
        this.userRepository = userRepository;
        this.assignmentRepository=complaintAssignmentRepository;
    }

    @Transactional
    public ResolutionResponse createResolution(
            Long complaintId,
            CreateResolutionRequest request,
            String authenticatedEmail
    ) {
        AppUser admin = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Administrator account not found"
                ));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can use this endpoint"
            );
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        return createResolutionForActor(complaint, request, admin);
    }

    @Transactional(readOnly = true)
    public ResolutionResponse getLatestResolutionForOwner(
            Long complaintId,
            String authenticatedEmail
    ){
        Complaint complaint=complaintRepository.findByIdAndUser_Email(complaintId,authenticatedEmail)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"Complaint not found"
                ));
        return complaintResolutionRepository
                .findAllByComplaint_IdOrderByResolvedAtDesc(complaint.getId())
                .stream()
                .findFirst()
                .map(ResolutionResponse::from)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No resolution has been recorded for this complaint"
                ));
    }

    @Transactional(readOnly = true)
    public List<ResolutionResponse> getAllResolutions(Long complaintId){
        if(!complaintRepository.existsById(complaintId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Complaint not found");
        }
        return complaintResolutionRepository
                .findAllByComplaint_IdOrderByResolvedAtDesc(complaintId)
                .stream()
                .map(ResolutionResponse::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public ComplaintResolution getResolutionById(
            Long complaintId,
            Long resolutionId
    ) {
        return complaintResolutionRepository
                .findByIdAndComplaint_Id(resolutionId, complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resolution not found for this complaint"
                ));
    }

    @Transactional
    public ResolutionResponse createResolutionForOfficial(
            Long complaintId,
            CreateResolutionRequest request,
            String authenticatedEmail
    ) {
        AppUser official = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Official account not found"
                ));

        if (official.getRole() != UserRole.OFFICIAL) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only officials can use this endpoint"
            );
        }

        if (official.getDepartment() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Official is not associated with a department"
            );
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        boolean currentlyAssigned = assignmentRepository
                .findByComplaint_IdAndDepartment_IdAndCurrentTrue(
                        complaintId,
                        official.getDepartment().getId()
                )
                .isPresent();

        if (!currentlyAssigned) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This complaint is not currently assigned to your department"
            );
        }

        return createResolutionForActor(complaint, request, official);
    }
    private ResolutionResponse createResolutionForActor(
            Complaint complaint,
            CreateResolutionRequest request,
            AppUser actor
    ) {
        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Complaint is already resolved"
            );
        }

        if (complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A rejected complaint cannot be resolved"
            );
        }

        ComplaintStatus previousStatus = complaint.getStatus();

        ComplaintResolution resolution = new ComplaintResolution();
        resolution.setComplaint(complaint);
        resolution.setResolutionNotes(request.resolutionNotes().trim());
        resolution.setResolvedBy(actor);

        resolution = complaintResolutionRepository.save(resolution);

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(ComplaintStatus.RESOLVED);
        history.setChangedBy(actor);
        history.setChangeReason(
                "Complaint resolved: " + request.resolutionNotes().trim()
        );

        complaintStatusHistoryRepository.save(history);

        return ResolutionResponse.from(resolution);
    }
}
