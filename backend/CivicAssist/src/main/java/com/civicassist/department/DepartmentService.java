package com.civicassist.department;

import com.civicassist.department.dto.CreateDepartmentRequest;
import com.civicassist.department.dto.DepartmentResponse;
import com.civicassist.department.dto.UpdateDepartmentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {

        String name = normalizeName(request.name());
        String code = normalizeCode(request.code());

        if (departmentRepository.existsByName(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A department with this name already exists"
            );
        }

        if (departmentRepository.existsByCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A department with this code already exists"
            );
        }

        Department department = new Department();
        department.setName(name);
        department.setCode(code);
        department.setDescription(normalizeDescription(request.description()));
        department.setActive(true);

        Department saved = departmentRepository.save(department);

        return DepartmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAllByOrderByNameAsc()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAllActive() {
        return departmentRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return DepartmentResponse.from(getDepartment(id));
    }

    @Transactional
    public DepartmentResponse update(
            Long id,
            UpdateDepartmentRequest request
    ) {
        Department department = getDepartment(id);

        String name = normalizeName(request.name());
        String code = normalizeCode(request.code());

        departmentRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A department with this name already exists"
                    );
                });

        departmentRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A department with this code already exists"
                    );
                });

        department.setName(name);
        department.setCode(code);
        department.setDescription(normalizeDescription(request.description()));

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse deactivate(Long id) {
        Department department = getDepartment(id);

        if (!department.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department is already inactive"
            );
        }

        department.setActive(false);

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse activate(Long id) {
        Department department = getDepartment(id);

        if (department.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department is already active"
            );
        }

        department.setActive(true);

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found"
                ));
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}