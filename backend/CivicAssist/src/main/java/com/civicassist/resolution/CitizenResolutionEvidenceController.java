package com.civicassist.resolution;

import com.civicassist.resolution.dto.ResolutionEvidenceResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/complaints/{complaintId}/resolutions/{resolutionId}/evidence")
public class CitizenResolutionEvidenceController {

    private final ResolutionEvidenceService evidenceService;

    public CitizenResolutionEvidenceController(
            ResolutionEvidenceService evidenceService
    ) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    public List<ResolutionEvidenceResponse> getEvidence(
            @PathVariable Long complaintId,
            @PathVariable Long resolutionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return evidenceService.getEvidenceForCitizen(
                        complaintId,
                        resolutionId,
                        jwt.getSubject()
                )
                .stream()
                .map(ResolutionEvidenceResponse::from)
                .toList();
    }

    @GetMapping("/{evidenceId}")
    public ResponseEntity<Resource> downloadEvidence(
            @PathVariable Long complaintId,
            @PathVariable Long resolutionId,
            @PathVariable Long evidenceId,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        ResolutionEvidence evidence =
                evidenceService.getEvidenceByIdForCitizen(
                        complaintId,
                        resolutionId,
                        evidenceId,
                        jwt.getSubject()
                );

        Resource resource = evidenceService.downloadEvidenceForCitizen(
                complaintId,
                resolutionId,
                evidenceId,
                jwt.getSubject()
        );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(evidence.getContentType())
                )
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