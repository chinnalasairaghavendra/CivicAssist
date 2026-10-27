package com.civicassist.assignment;

import com.civicassist.assignment.dto.AssignDepartmentRequest;
import com.civicassist.assignment.dto.AssignmentResponse;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.department.Department;
import com.civicassist.department.DepartmentRepository;
import com.civicassist.user.AppUser;
import com.civicassist.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ComplaintAssignmentService {
    private final ComplaintAssignmentRepository assignmentRepository;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public ComplaintAssignmentService(
            ComplaintAssignmentRepository assignmentRepository,
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ){
        this.assignmentRepository=assignmentRepository;
        this.complaintRepository=complaintRepository;
        this.userRepository=userRepository;
        this.departmentRepository=departmentRepository;
    }

    @Transactional
    public AssignmentResponse assign(
            Long complaintId,
            AssignDepartmentRequest request,
            String authenticatedEmail
    ){
        String departmentCode = request.departmentCode()
                .trim()
                .toUpperCase(Locale.ROOT);

        Department department = departmentRepository
                .findByCode(departmentCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid department code"
                ));
        if (!department.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot assign a complaint to an inactive department"
            );
        }
        Complaint complaint= complaintRepository.findById(complaintId)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));
        AppUser admin=userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Administrator account not found"));
        assignmentRepository.findByComplaint_IdAndCurrentTrue(complaintId)
                .ifPresent(existing->{
                    existing.setCurrent(false);
                    assignmentRepository.saveAndFlush(existing);
                });
        ComplaintAssignment assignment=new ComplaintAssignment();
        assignment.setComplaint(complaint);
        assignment.setDepartment(department);
        assignment.setAssignedBy(admin);
        assignment.setAssignmentNote(
                request.assignmentNote()==null?null:request.assignmentNote().trim());
        assignment.setCurrent(true);
        ComplaintAssignment saved=assignmentRepository.save(assignment);
        return AssignmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getHistory(Long complaintId){
        if(!complaintRepository.existsById(complaintId))
        {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Complaint not found"
            );
        }
        return assignmentRepository
                .findAllByComplaint_IdOrderByAssignedAtDesc(complaintId)
                .stream()
                .map(AssignmentResponse::from)
                .toList();
    }
}
