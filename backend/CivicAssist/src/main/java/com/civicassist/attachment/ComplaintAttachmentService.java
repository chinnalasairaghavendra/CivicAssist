package com.civicassist.attachment;

import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintAttachment;
import com.civicassist.complaint.ComplaintAttachmentRepository;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.dto.ComplaintAttachmentResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ComplaintAttachmentService {

    private static final int MAX_ATTACHMENTS_PER_COMPLAINT = 5;

    private final ComplaintRepository complaintRepository;
    private final ComplaintAttachmentRepository attachmentRepository;
    private final LocalAttachmentStorageService storageService;

    public ComplaintAttachmentService(
            ComplaintRepository complaintRepository,
            ComplaintAttachmentRepository attachmentRepository,
            LocalAttachmentStorageService storageService
    ) {
        this.complaintRepository = complaintRepository;
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
    }

    @Transactional
    public ComplaintAttachmentResponse upload(
            Long complaintId,
            String email,
            MultipartFile file
    ) {
        Complaint complaint = complaintRepository
                .findByIdAndUser_Email(complaintId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        long attachmentCount =
                attachmentRepository.countByComplaint_Id(complaintId);

        if (attachmentCount >= MAX_ATTACHMENTS_PER_COMPLAINT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A complaint can have at most 5 attachments"
            );
        }

        LocalAttachmentStorageService.StoredFile storedFile;

        try {
            storedFile = storageService.store(file);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store attachment"
            );
        }

        try {
            ComplaintAttachment attachment = new ComplaintAttachment();

            attachment.setComplaint(complaint);
            attachment.setOriginalFilename(
                    sanitizeOriginalFilename(file.getOriginalFilename())
            );
            attachment.setStoredFilename(storedFile.filename());
            attachment.setContentType(storedFile.contentType());
            attachment.setFileSize(storedFile.size());

            ComplaintAttachment saved =
                    attachmentRepository.saveAndFlush(attachment);

            return ComplaintAttachmentResponse.from(saved);

        } catch (RuntimeException ex) {
            try {
                storageService.delete(storedFile.filename());
            } catch (IOException cleanupException) {
                ex.addSuppressed(cleanupException);
            }

            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ComplaintAttachmentResponse> listForUser(
            Long complaintId,
            String email
    ) {
        complaintRepository.findByIdAndUser_Email(complaintId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        return attachmentRepository
                .findAllByComplaint_IdOrderByUploadedAtDesc(complaintId)
                .stream()
                .map(ComplaintAttachmentResponse::from)
                .toList();
    }

    private String sanitizeOriginalFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "attachment";
        }

        String normalized = filename.replace('\\', '/');

        int lastSlash = normalized.lastIndexOf('/');

        if (lastSlash >= 0) {
            normalized = normalized.substring(lastSlash + 1);
        }

        normalized = normalized.replaceAll("[\\p{Cntrl}]", "_");

        if (normalized.length() > 255) {
            normalized = normalized.substring(
                    normalized.length() - 255
            );
        }

        return normalized.isBlank() ? "attachment" : normalized;
    }

    @Transactional(readOnly = true)
    public Resource download(
            Long complaintId,
            Long attachmentId,
            String email
    ) {
        // Verify that the complaint belongs to the logged-in user
        Complaint complaint = complaintRepository
                .findByIdAndUser_Email(complaintId, email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Complaint not found"
                        )
                );

        // Verify that the attachment belongs to this complaint
        ComplaintAttachment attachment = attachmentRepository
                .findByIdAndComplaint_Id(attachmentId, complaint.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Attachment not found"
                        )
                );

        try {
            Path path = storageService.resolve(attachment.getStoredFilename());

            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment file not found on storage"
                );
            }

            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment file is not readable"
                );
            }

            return resource;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not read attachment"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ComplaintAttachmentResponse> listForAdmin(Long complaintId) {
        complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        return attachmentRepository
                .findAllByComplaint_IdOrderByUploadedAtDesc(complaintId)
                .stream()
                .map(ComplaintAttachmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadForAdmin(
            Long complaintId,
            Long attachmentId
    ) {
        complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        ComplaintAttachment attachment = attachmentRepository
                .findByIdAndComplaint_Id(attachmentId, complaintId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment not found"
                ));

        try {
            Path path = storageService.resolve(
                    attachment.getStoredFilename()
            );

            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment file not found"
                );
            }

            return new UrlResource(path.toUri());

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not read attachment"
            );
        }
    }
}