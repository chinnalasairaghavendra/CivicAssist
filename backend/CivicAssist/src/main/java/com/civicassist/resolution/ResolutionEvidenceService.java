package com.civicassist.resolution;

import com.civicassist.attachment.LocalAttachmentStorageService;
import com.civicassist.attachment.LocalAttachmentStorageService.StoredFile;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ResolutionEvidenceService {

    private static final int MAX_FILES_PER_RESOLUTION = 5;

    private final ComplaintRepository complaintRepository;
    private final ResolutionEvidenceRepository evidenceRepository;
    private final ComplaintResolutionRepository resolutionRepository;
    private final LocalAttachmentStorageService storageService;

    public ResolutionEvidenceService(
            ResolutionEvidenceRepository evidenceRepository,
            ComplaintResolutionRepository resolutionRepository,
            LocalAttachmentStorageService storageService,
            ComplaintRepository complaintRepository
    ) {
        this.evidenceRepository = evidenceRepository;
        this.resolutionRepository = resolutionRepository;
        this.storageService = storageService;
        this.complaintRepository=complaintRepository;
    }

    @Transactional
    public ResolutionEvidence uploadEvidence(
            Long complaintId,
            Long resolutionId,
            MultipartFile file
    ) throws IOException {

        ComplaintResolution resolution = resolutionRepository
                .findByIdAndComplaint_Id(resolutionId, complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resolution not found for this complaint"
                ));

        long existingFiles =
                evidenceRepository.countByResolution_Id(resolutionId);

        if (existingFiles >= MAX_FILES_PER_RESOLUTION) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A resolution can have at most 5 evidence files"
            );
        }

        StoredFile storedFile = storageService.store(file);

        try {
            ResolutionEvidence evidence = new ResolutionEvidence();

            evidence.setResolution(resolution);
            evidence.setOriginalFilename(
                    sanitizeFilename(file.getOriginalFilename())
            );
            evidence.setStoredFilename(storedFile.filename());
            evidence.setContentType(storedFile.contentType());
            evidence.setFileSize(storedFile.size());

            return evidenceRepository.save(evidence);

        } catch (RuntimeException ex) {
            // Avoid leaving a physical file behind if saving metadata fails.
            try {
                storageService.delete(storedFile.filename());
            } catch (IOException cleanupException) {
                ex.addSuppressed(cleanupException);
            }

            throw ex;
        }
    }

    @Transactional
    public List<ResolutionEvidence> getEvidence(Long resolutionId) {

        if (!resolutionRepository.existsById(resolutionId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Resolution not found"
            );
        }

        return evidenceRepository
                .findAllByResolution_IdOrderByUploadedAtDesc(resolutionId);
    }

    @Transactional
    public Resource downloadEvidence(
            Long complaintId,
            Long resolutionId,
            Long evidenceId
    ) throws IOException {

        ResolutionEvidence evidence = evidenceRepository
                .findByIdAndResolution_Id(evidenceId, resolutionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Evidence not found"
                ));

        if (!evidence.getResolution().getComplaint().getId()
                .equals(complaintId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Evidence not found for this complaint"
            );
        }

        Path path = storageService.resolve(evidence.getStoredFilename());

        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Evidence file not found on storage"
            );
        }

        return new UrlResource(path.toUri());
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "evidence";
        }

        // Remove any client-supplied directory path.
        String sanitized = filename.replace('\\', '/');
        sanitized = sanitized.substring(sanitized.lastIndexOf('/') + 1);

        if (sanitized.isBlank()) {
            return "evidence";
        }

        return sanitized.length() > 255
                ? sanitized.substring(0, 255)
                : sanitized;
    }

    @Transactional(readOnly = true)
    public List<ResolutionEvidence> getEvidenceForComplaint(
            Long complaintId,
            Long resolutionId
    ) {
        resolutionRepository
                .findByIdAndComplaint_Id(resolutionId, complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resolution not found for this complaint"
                ));

        return evidenceRepository
                .findAllByResolution_IdOrderByUploadedAtDesc(resolutionId);
    }

    @Transactional(readOnly = true)
    public ResolutionEvidence getEvidenceById(
            Long complaintId,
            Long resolutionId,
            Long evidenceId
    ) {
        resolutionRepository
                .findByIdAndComplaint_Id(resolutionId, complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resolution not found for this complaint"
                ));

        return evidenceRepository
                .findByIdAndResolution_Id(evidenceId, resolutionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Evidence not found"
                ));
    }

    private void verifyComplaintOwnership(Long complaintId, String email) {
        complaintRepository.findByIdAndUser_Email(complaintId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<ResolutionEvidence> getEvidenceForCitizen(
            Long complaintId,
            Long resolutionId,
            String email
    ) {
        verifyComplaintOwnership(complaintId, email);

        return getEvidenceForComplaint(complaintId, resolutionId);
    }

    @Transactional(readOnly = true)
    public Resource downloadEvidenceForCitizen(
            Long complaintId,
            Long resolutionId,
            Long evidenceId,
            String email
    ) throws IOException {

        verifyComplaintOwnership(complaintId, email);

        return downloadEvidence(complaintId, resolutionId, evidenceId);
    }

    @Transactional(readOnly = true)
    public ResolutionEvidence getEvidenceByIdForCitizen(
            Long complaintId,
            Long resolutionId,
            Long evidenceId,
            String email
    ) {
        verifyComplaintOwnership(complaintId, email);

        return getEvidenceById(
                complaintId,
                resolutionId,
                evidenceId
        );
    }
}