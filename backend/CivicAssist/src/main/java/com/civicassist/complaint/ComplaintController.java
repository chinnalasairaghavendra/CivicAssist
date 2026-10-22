package com.civicassist.complaint;

import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.complaint.dto.CreateComplaintRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service)
    {
        this.service=service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintResponse create(@Valid @RequestBody CreateComplaintRequest request)
    {
        return service.create(request);
    }

    @GetMapping
    public List<ComplaintResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ComplaintResponse findById(@PathVariable Long id)
    {
        return service.findById(id);
    }
}
