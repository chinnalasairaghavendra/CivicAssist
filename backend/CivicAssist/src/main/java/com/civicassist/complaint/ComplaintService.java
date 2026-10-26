package com.civicassist.complaint;

import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.complaint.dto.CreateComplaintRequest;
import com.civicassist.complaint.dto.CreateComplaintResponse;
import com.civicassist.user.AppUser;
import com.civicassist.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final DuplicateComplaintService duplicateComplaintService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            DuplicateComplaintService duplicateComplaintService){
        this.complaintRepository=complaintRepository;
        this.userRepository=userRepository;
        this.duplicateComplaintService=duplicateComplaintService;
    }

    @Transactional
    public CreateComplaintResponse create(
            CreateComplaintRequest request,
            String email
    ) {
        validateCoordinates(request.latitude(), request.longitude());

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        // Find possible duplicates before saving the new complaint.
        var potentialDuplicates =
                duplicateComplaintService.findPotentialDuplicates(
                        request.title().trim(),
                        request.description().trim(),
                        request.category(),
                        request.address(),
                        request.latitude(),
                        request.longitude()
                );

        Complaint complaint = new Complaint();
        complaint.setTitle(request.title().trim());
        complaint.setDescription(request.description().trim());
        complaint.setAddress(request.address());
        complaint.setCategory(request.category());
        complaint.setLatitude(request.latitude());
        complaint.setLongitude(request.longitude());
        complaint.setUser(user);

        Complaint savedComplaint = complaintRepository.save(complaint);

        return new CreateComplaintResponse(
                ComplaintResponse.from(savedComplaint),
                !potentialDuplicates.isEmpty(),
                potentialDuplicates
        );
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> findAllForUser(String email){
        return complaintRepository.findAllByUser_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(ComplaintResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComplaintResponse findByIdForUser(Long id,String email){
        Complaint complaint=complaintRepository.findByIdAndUser_Email(id,email).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Complaint not found"));
        return ComplaintResponse.from(complaint);
    }

    private void validateCoordinates(Double latitude,Double longitude)
    {
        if((latitude==null)!=(longitude==null))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Latitude and longitude must be provided together");
        }
        if(latitude!=null&&(latitude<-90||latitude>90||longitude<-180||longitude>180)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid latitude or longitude");
        }
    }
}
