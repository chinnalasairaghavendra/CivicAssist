package com.civicassist.complaint;

import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.complaint.dto.CreateComplaintRequest;
import com.civicassist.complaint.dto.CreateComplaintResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service)
    {
        this.service=service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateComplaintResponse create(
            @Valid @RequestBody CreateComplaintRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.create(request, jwt.getSubject());
    }

    @GetMapping
    public List<ComplaintResponse> findAll(@AuthenticationPrincipal Jwt jwt){
        return service.findAllForUser(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public ComplaintResponse findById(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt)
    {
        return service.findByIdForUser(id,jwt.getSubject());
    }
}
