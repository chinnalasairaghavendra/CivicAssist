package com.civicassist.assignment;

import com.civicassist.assignment.dto.AssignDepartmentRequest;
import com.civicassist.assignment.dto.AssignmentResponse;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.department.Department;
import com.civicassist.department.DepartmentRepository;
import com.civicassist.history.ComplaintStatusHistory;
import com.civicassist.history.ComplaintStatusHistoryRepository;
import com.civicassist.notification.entity.NotificationType;
import com.civicassist.notification.service.NotificationService;
import com.civicassist.user.AppUser;
import com.civicassist.user.UserRepository;
import com.civicassist.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeException;
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
    private final NotificationService notificationService;
    private final ComplaintStatusHistoryRepository historyRepository;

    public ComplaintAssignmentService(
            ComplaintAssignmentRepository assignmentRepository,
            ComplaintRepository complaintRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            NotificationService notificationService,
            ComplaintStatusHistoryRepository historyRepository
    ){
        this.assignmentRepository=assignmentRepository;
        this.complaintRepository=complaintRepository;
        this.userRepository=userRepository;
        this.departmentRepository=departmentRepository;
        this.notificationService=notificationService;
        this.historyRepository=historyRepository;
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
//        assignmentRepository.findByComplaint_IdAndCurrentTrue(complaintId)
//                .ifPresent(existing->{
//                    existing.setCurrent(false);
//                    assignmentRepository.saveAndFlush(existing);
//                });
        if(complaint.getStatus() == ComplaintStatus.RESOLVED||complaint.getStatus()==ComplaintStatus.REJECTED)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Cannot assign a resolved or rejected complaint");
        }
        var existingAssignment=assignmentRepository.findByComplaint_IdAndCurrentTrue(complaintId);
        if(existingAssignment.isPresent())
        {
            ComplaintAssignment existing=existingAssignment.get();
            if(existing.getDepartment().getId().equals(department.getId())){
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This department is already assigned to the complaint.");
            }
            existing.setCurrent(false);
            assignmentRepository.saveAndFlush(existing);
        }

        ComplaintAssignment assignment=new ComplaintAssignment();
        assignment.setComplaint(complaint);
        assignment.setDepartment(department);
        assignment.setAssignedBy(admin);
        assignment.setAssignmentNote(
                request.assignmentNote()==null?null:request.assignmentNote().trim());
        assignment.setCurrent(true);
        ComplaintAssignment saved=assignmentRepository.save(assignment);

        //Automatically transition to assigned
        ComplaintStatus previousStatus=complaint.getStatus();
        if(previousStatus!=ComplaintStatus.ASSIGNED)
        {
            complaint.setStatus(ComplaintStatus.ASSIGNED);
            Complaint savedComplaint=complaintRepository.save(complaint);
            ComplaintStatusHistory history=new ComplaintStatusHistory();
            history.setComplaint(savedComplaint);
            history.setPreviousStatus(previousStatus);
            history.setNewStatus(ComplaintStatus.ASSIGNED);
            history.setChangedBy(admin);
            history.setChangeReason(
                    "Automatically changed to ASSIGNED after department assignment."
            );
            historyRepository.save(history);
        }
        List<AppUser> officials=userRepository.findAllByDepartment_IdAndRole(department.getId(), UserRole.OFFICIAL);
        for(AppUser official:officials)
        {
            notificationService.createNotification(official.getId(),complaint.getId(),"New Complaint has been assigned",
                    "Complaint #"+complaint.getId()+" has been assigned to your department.",NotificationType.COMPLAINT_ASSIGNED);
        }
        notificationService.createNotification(complaint.getUser().getId(),
                complaint.getId(),
                "Complaint Assigned",
                "Complaint has been assigned to the "+department.getName()+" department.",
                NotificationType.COMPLAINT_ASSIGNED);
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
