package com.civicassist.complaint;

import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.complaint.dto.CreateComplaintRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ComplaintService {
    private final ComplaintRepository repository;

    public ComplaintService(ComplaintRepository repository){
        this.repository=repository;
    }

    @Transactional
    public ComplaintResponse create(CreateComplaintRequest request){
        validateCoordinates(request.latitude(),request.longitude());
        Complaint complaint=new Complaint();
        complaint.setTitle(request.title().trim());
        complaint.setDescription(request.description().trim());
        complaint.setAddress(request.address());
        complaint.setCategory(request.category());
        complaint.setLatitude(request.latitude());
        complaint.setLongitude(request.longitude());
        return ComplaintResponse.from(repository.save(complaint));
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> findAll(){
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ComplaintResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComplaintResponse findById(Long id){
        Complaint complaint=repository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Complaint not found"));
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
