package com.civicassist.attachment;

import com.civicassist.complaint.dto.ComplaintAttachmentResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/complaints/{complaintId}/attachments")
public class ComplaintAttachmentController {

    private final ComplaintAttachmentService attachmentService;

    public ComplaintAttachmentController(
            ComplaintAttachmentService attachmentService
    ) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintAttachmentResponse upload(
            @PathVariable Long complaintId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return attachmentService.upload(
                complaintId,
                jwt.getSubject(),
                file
        );
    }

    @GetMapping
    public List<ComplaintAttachmentResponse> list(
            @PathVariable Long complaintId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return attachmentService.listForUser(
                complaintId,
                jwt.getSubject()
        );
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> download(
            @PathVariable Long complaintId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Resource resource = attachmentService.download(
                complaintId,
                attachmentId,
                jwt.getSubject()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\""
                )
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(resource);
    }
}