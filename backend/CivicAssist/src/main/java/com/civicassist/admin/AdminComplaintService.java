package com.civicassist.admin;

import com.civicassist.admin.dto.UpdateComplaintStatusRequest;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.dto.ComplaintResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminComplaintService {
    private final ComplaintRepository complaintRepository;

    public AdminComplaintService(ComplaintRepository complaintRepository){
        this.complaintRepository=complaintRepository;
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> findAllComplaints(){
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ComplaintResponse::from)
                .toList();
    }

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId,
                                          UpdateComplaintStatusRequest request){
        Complaint complaint=complaintRepository.findById(complaintId).
                orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Complaint not found"));
        complaint.setStatus(request.status());
        Complaint saved=complaintRepository.save(complaint);
        return ComplaintResponse.from(saved);
    }
}
