package com.civicassist.attachment;

import com.civicassist.complaint.dto.ComplaintAttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints/{complaintId}/attachments")
public class AdminComplaintAttachmentController {

    private final ComplaintAttachmentService attachmentService;

    public AdminComplaintAttachmentController(
            ComplaintAttachmentService attachmentService
    ) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public ResponseEntity<List<ComplaintAttachmentResponse>> list(
            @PathVariable Long complaintId
    ) {
        return ResponseEntity.ok(
                attachmentService.listForAdmin(complaintId)
        );
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> download(
            @PathVariable Long complaintId,
            @PathVariable Long attachmentId
    ) {
        Resource resource = attachmentService.downloadForAdmin(
                complaintId,
                attachmentId
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + resource.getFilename() + "\""
                )
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(resource);
    }
}