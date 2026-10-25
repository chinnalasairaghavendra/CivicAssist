package com.civicassist.admin;

import com.civicassist.admin.dto.UpdateComplaintStatusRequest;
import com.civicassist.complaint.ComplaintController;
import com.civicassist.complaint.dto.ComplaintResponse;
import jakarta.validation.Valid;
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
    public List<ComplaintResponse> getAllComplaints(){
        return adminComplaintService.findAllComplaints();
    }

    @PutMapping("/{id}/status")
    public ComplaintResponse updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintStatusRequest request
            )
    {
        return adminComplaintService.updateStatus(id,request);
    }
}
