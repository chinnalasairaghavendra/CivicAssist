package com.civicassist.resolution;


import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.history.ComplaintStatusHistory;
import com.civicassist.history.ComplaintStatusHistoryRepository;
import com.civicassist.resolution.dto.CreateResolutionRequest;
import com.civicassist.resolution.dto.ResolutionResponse;
import com.civicassist.user.AppUser;
import com.civicassist.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@Service
public class ComplaintResolutionService {
    private final ComplaintRepository complaintRepository;
    private final ComplaintResolutionRepository complaintResolutionRepository;
    private final ComplaintStatusHistoryRepository complaintStatusHistoryRepository;
    private final UserRepository userRepository;

    public ComplaintResolutionService(
            ComplaintRepository complaintRepository,
            ComplaintResolutionRepository resolutionRepository,
            ComplaintStatusHistoryRepository historyRepository,
            UserRepository userRepository) {

        this.complaintRepository = complaintRepository;
        this.complaintResolutionRepository = resolutionRepository;
        this.complaintStatusHistoryRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResolutionResponse createResolution(
            Long complaintId,
            CreateResolutionRequest request,
            String authenticatedEmail) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Complaint not found"));

        AppUser admin = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Administrator account not found"));

        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Complaint is already resolved");
        }

        if (complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A rejected complaint cannot be resolved");
        }

        String evidenceUrl = normalizeEvidenceUrl(request.evidenceUrl());

        // Capture the original status before changing it.
        ComplaintStatus previousStatus = complaint.getStatus();

        // 1. Create the resolution record.
        ComplaintResolution resolution = new ComplaintResolution();
        resolution.setComplaint(complaint);
        resolution.setResolutionNotes(request.resolutionNotes().trim());
        resolution.setEvidenceUrl(evidenceUrl);
        resolution.setResolvedBy(admin);

        resolution = complaintResolutionRepository.save(resolution);

        // 2. Update the complaint status.
        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaintRepository.save(complaint);

        // 3. Record the status transition.
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(ComplaintStatus.RESOLVED);
        history.setChangedBy(admin);
        history.setChangeReason(
                "Complaint resolved: " + request.resolutionNotes().trim());

        complaintStatusHistoryRepository.save(history);

        return ResolutionResponse.from(resolution);
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

    private String normalizeEvidenceUrl(String value) {
        if (value == null || value.isBlank())
            return null;

        String url = value.trim();

        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (!uri.isAbsolute()
                    || scheme == null
                    || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
            return uri.toASCIIString();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Evidence URL must be a valid HTTP or HTTPS URL"
            );
        }
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
}
