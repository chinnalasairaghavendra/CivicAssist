package com.civicassist.triage;

import com.civicassist.complaint.dto.ComplaintResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/complaints")
public class TriageController {

    private final TriageService triageService;
    public TriageController(TriageService triageService){
        this.triageService=triageService;
    }

    @PostMapping("/{id}/triage")
    public ComplaintResponse triage(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return triageService.triage(id,jwt.getSubject());
    }


}
