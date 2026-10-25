package com.civicassist.assignment;

import com.civicassist.assignment.dto.AssignDepartmentRequest;
import com.civicassist.assignment.dto.AssignmentResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
public class ComplaintAssignmentController {
    private final ComplaintAssignmentService assignmentService;

    public ComplaintAssignmentController(
            ComplaintAssignmentService assignmentService
    ){
        this.assignmentService=assignmentService;
    }

    @PostMapping("/{id}/assign")
    public AssignmentResponse assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignDepartmentRequest request,
            @AuthenticationPrincipal Jwt jwt
            ){
        return assignmentService.assign(
                id,
                request,
                jwt.getSubject()
        );
    }

    @GetMapping("/{id}/assignments")
    public List<AssignmentResponse> getAssignmentHistory(
            @PathVariable Long id
    ){
        return assignmentService.getHistory(id);
    }
}
