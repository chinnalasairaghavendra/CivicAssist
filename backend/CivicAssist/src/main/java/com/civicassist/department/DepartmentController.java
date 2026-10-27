package com.civicassist.department;

import com.civicassist.department.dto.CreateDepartmentRequest;
import com.civicassist.department.dto.DepartmentResponse;
import com.civicassist.department.dto.UpdateDepartmentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // Create a new department
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(
            @Valid @RequestBody CreateDepartmentRequest request
    ) {
        DepartmentResponse response = departmentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Retrieve all departments, including inactive ones
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    // Retrieve only active departments
    @GetMapping("/active")
    public ResponseEntity<List<DepartmentResponse>> findAllActive() {
        return ResponseEntity.ok(departmentService.findAllActive());
    }

    // Retrieve a department by ID
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    // Update department details
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        return ResponseEntity.ok(
                departmentService.update(id, request)
        );
    }

    // Deactivate a department
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<DepartmentResponse> deactivate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                departmentService.deactivate(id)
        );
    }

    // Reactivate a department
    @PatchMapping("/{id}/activate")
    public ResponseEntity<DepartmentResponse> activate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                departmentService.activate(id)
        );
    }
}