package com.civicassist.user;

import com.civicassist.department.Department;
import com.civicassist.department.DepartmentRepository;
import com.civicassist.user.dto.CreateOfficialRequest;
import com.civicassist.user.dto.OfficialResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class OfficialManagementService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficialManagementService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public OfficialResponse createOfficial(CreateOfficialRequest request) {

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

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
                    "Cannot create an official for an inactive department"
            );
        }

        String encodedPassword = passwordEncoder.encode(
                request.password()
        );

        AppUser official = new AppUser(
                request.name().trim(),
                email,
                encodedPassword
        );

        official.setRole(UserRole.OFFICIAL);
        official.setDepartment(department);

        AppUser savedOfficial = userRepository.save(official);

        return OfficialResponse.from(savedOfficial);
    }
}