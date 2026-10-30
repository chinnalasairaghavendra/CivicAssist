package com.civicassist.resolution;

import com.civicassist.resolution.dto.CreateResolutionRequest;
import com.civicassist.resolution.dto.ResolutionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ResolutionController {
    private final ComplaintResolutionService resolutionService;

    public ResolutionController(
            ComplaintResolutionService resolutionService
    ){
        this.resolutionService=resolutionService;
    }

    @PostMapping("/api/admin/complaints/{id}/resolution")
    public ResolutionResponse createResolution(
            @PathVariable Long id,
            @Valid @RequestBody CreateResolutionRequest request,
            @AuthenticationPrincipal Jwt jwt
            ){
        return resolutionService.createResolution(
                id,
                request,
                jwt.getSubject()
        );
    }

    @GetMapping("/api/complaints/{id}/resolution")
    public ResolutionResponse getResolution(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ){
        return resolutionService.getLatestResolutionForOwner(
                id,
                jwt.getSubject()
        );
    }

    @GetMapping("/api/admin/complaints/{id}/resolutions")
    public List<ResolutionResponse> getAllResolutions(
            @PathVariable Long id
    ){
        return resolutionService.getAllResolutions(id);
    }

    @PostMapping("/api/official/complaints/{id}/resolution")
    @ResponseStatus(HttpStatus.CREATED)
    public ResolutionResponse createOfficialResolution(
            @PathVariable Long id,
            @Valid @RequestBody CreateResolutionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return resolutionService.createResolutionForOfficial(
                id,
                request,
                jwt.getSubject()
        );
    }
}
