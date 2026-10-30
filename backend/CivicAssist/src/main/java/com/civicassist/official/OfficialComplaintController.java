package com.civicassist.official;

import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.official.dto.OfficialStatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/official/complaints")
public class OfficialComplaintController {

    private final OfficialComplaintService officialComplaintService;

    public OfficialComplaintController(
            OfficialComplaintService officialComplaintService
    ) {
        this.officialComplaintService = officialComplaintService;
    }

    @GetMapping
    public ResponseEntity<Page<ComplaintResponse>> getDepartmentComplaints(
            Authentication authentication,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                officialComplaintService.getDepartmentComplaints(
                        authentication.getName(),
                        status,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<ComplaintResponse> getComplaint(
            @PathVariable Long complaintId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                officialComplaintService.getComplaint(
                        complaintId,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{complaintId}/status")
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable Long complaintId,
            Authentication authentication,
            @Valid @RequestBody OfficialStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
                officialComplaintService.updateStatus(
                        complaintId,
                        authentication.getName(),
                        request.status(),
                        request.changeReason()
                )
        );
    }
}