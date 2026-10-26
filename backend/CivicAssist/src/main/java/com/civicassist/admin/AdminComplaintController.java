package com.civicassist.admin;

import com.civicassist.admin.dto.UpdateComplaintStatusRequest;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.history.dto.ComplaintStatusHistoryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {
    private final AdminComplaintService adminComplaintService;

    public AdminComplaintController(AdminComplaintService adminComplaintService)
    {
        this.adminComplaintService=adminComplaintService;
    }

    @GetMapping
    public Page<ComplaintResponse> getAllComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return adminComplaintService.findComplaints(
                page,
                size,
                status,
                category,
                keyword,
                sortBy,
                direction
        );
    }

    @PutMapping("/{id}/status")
    public ComplaintResponse updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintStatusRequest request,
            @AuthenticationPrincipal Jwt jwt
            )
    {
        return adminComplaintService.updateStatus(
                id,
                request,
                jwt.getSubject()
        );
    }

    @GetMapping("/{id}/status-history")
    public List<ComplaintStatusHistoryResponse> getStatusHistory(
            @PathVariable Long id
    ){
        return adminComplaintService.getStatusHistory(id);
    }
}
