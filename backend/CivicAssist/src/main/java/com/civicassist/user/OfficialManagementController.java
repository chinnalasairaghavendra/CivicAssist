package com.civicassist.user;

import com.civicassist.user.dto.CreateOfficialRequest;
import com.civicassist.user.dto.OfficialResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/officials")
public class OfficialManagementController {

    private final OfficialManagementService officialManagementService;

    public OfficialManagementController(
            OfficialManagementService officialManagementService
    ) {
        this.officialManagementService = officialManagementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfficialResponse createOfficial(
            @Valid @RequestBody CreateOfficialRequest request
    ) {
        return officialManagementService.createOfficial(request);
    }
}