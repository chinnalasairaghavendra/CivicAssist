package com.civicassist.resolution;

import com.civicassist.resolution.dto.ResolutionEvidenceResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class OfficialResolutionEvidenceController {
    private final ResolutionEvidenceService evidenceService;

    public OfficialResolutionEvidenceController(ResolutionEvidenceService evidenceService){
        this.evidenceService=evidenceService;
    }
    @PostMapping(
            value = "/api/official/complaints/{complaintId}/resolutions/{resolutionId}/evidence",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResolutionEvidenceResponse uploadEvidenceForOfficial(
            @PathVariable Long complaintId,
            @PathVariable Long resolutionId,
            @RequestParam("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        return ResolutionEvidenceResponse.from(
                evidenceService.uploadEvidenceForOfficial(
                        complaintId,
                        resolutionId,
                        file,
                        jwt.getSubject()
                )
        );
    }
}
