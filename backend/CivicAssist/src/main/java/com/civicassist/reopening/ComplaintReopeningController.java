package com.civicassist.reopening;

import com.civicassist.reopening.dto.CreateReopeningRequest;
import com.civicassist.reopening.dto.ReopeningRequestResponse;
import com.civicassist.reopening.dto.ReviewReopeningRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ComplaintReopeningController {

    private final ComplaintReopeningService reopeningService;

    public ComplaintReopeningController(
            ComplaintReopeningService reopeningService
    ) {
        this.reopeningService = reopeningService;
    }

    @PostMapping("/api/complaints/{complaintId}/reopening-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ReopeningRequestResponse createRequest(
            @PathVariable Long complaintId,
            @Valid @RequestBody CreateReopeningRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return reopeningService.createRequest(
                complaintId,
                request,
                jwt.getSubject()
        );
    }

    @GetMapping("/api/complaints/{complaintId}/reopening-requests")
    public List<ReopeningRequestResponse> getCitizenRequests(
            @PathVariable Long complaintId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return reopeningService.getCitizenRequests(
                complaintId,
                jwt.getSubject()
        );
    }

    @GetMapping("/api/admin/reopening-requests/pending")
    public List<ReopeningRequestResponse> getPendingRequests() {
        return reopeningService.getPendingRequests();
    }

    @PatchMapping("/api/admin/reopening-requests/{requestId}")
    public ReopeningRequestResponse reviewRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody ReviewReopeningRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return reopeningService.reviewRequest(
                requestId,
                request,
                jwt.getSubject()
        );
    }
}