package com.civicassist.resolution;

import com.civicassist.resolution.dto.ResolutionEvidenceResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints/{complaintId}/resolutions/{resolutionId}/evidence")
public class ResolutionEvidenceController {

    private final ResolutionEvidenceService evidenceService;

    public ResolutionEvidenceController(
            ResolutionEvidenceService evidenceService
    ) {
        this.evidenceService = evidenceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResolutionEvidenceResponse uploadEvidence(
            @PathVariable Long complaintId,
            @PathVariable Long resolutionId,
            @RequestParam("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        return ResolutionEvidenceResponse.from(
                evidenceService.uploadEvidence(
                        complaintId,
                        resolutionId,
                        file
                )
        );
    }

    @GetMapping
    public List<ResolutionEvidenceResponse> getEvidence(
            @PathVariable Long complaintId,
            @PathVariable Long resolutionId
    ) {
        return evidenceService.getEvidenceForComplaint(
                        complaintId,
                        resolutionId
                )
                .stream()
                .map(ResolutionEvidenceResponse::from)
                .toList();
    }

    @GetMapping("/{evidenceId}")
    public ResponseEntity<Resource> downloadEvidence(
            @PathVariable Long complaintId,
            @PathVariable Long resolutionId,
            @PathVariable Long evidenceId
    ) throws IOException {

        ResolutionEvidence evidence = evidenceService.getEvidenceById(
                complaintId,
                resolutionId,
                evidenceId
        );

        Resource resource = evidenceService.downloadEvidence(
                complaintId,
                resolutionId,
                evidenceId
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(evidence.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(evidence.getOriginalFilename())
                                .build()
                                .toString()
                )
                .body(resource);
    }
}