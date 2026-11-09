package com.civicassist.triage;

import com.civicassist.complaint.AiTriageStatus;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.department.Department;
import com.civicassist.department.DepartmentRepository;
import com.civicassist.triage.dto.TriageSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TriageService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;
    private final TriageClassifier triageClassifier;
    private static final Logger log= LoggerFactory.getLogger(TriageService.class);

    @Value("${spring.ai.google.genai.chat.model}")
    private String aiModel;

    public TriageService(
            ComplaintRepository complaintRepository,
            DepartmentRepository departmentRepository,
            TriageClassifier triageClassifier
    ){
        this.complaintRepository=complaintRepository;
        this.departmentRepository=departmentRepository;
        this.triageClassifier=triageClassifier;
    }


    public ComplaintResponse triage(Long complaintId, String authenticatedEmail) {

        log.info("AI triage started for complaint ID: {}", complaintId);

        Complaint complaint = complaintRepository
                .findByIdAndUser_Email(complaintId, authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found"
                ));

        List<Department> activeDepartments =
                departmentRepository.findAllByActiveTrueOrderByNameAsc();

        if (activeDepartments.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No active departments are available for complaint triage"
            );
        }

        Set<String> allowedDepartmentNames = activeDepartments.stream()
                .map(Department::getName)
                .collect(Collectors.toSet());

        // Record the attempt
        complaint.setAiTriageStatus(AiTriageStatus.PROCESSING);
        complaint.setAiTriageAttempts(
                complaint.getAiTriageAttempts() == null
                        ? 1
                        : complaint.getAiTriageAttempts() + 1
        );
        complaint.setAiLastAttemptAt(OffsetDateTime.now());
        complaint.setAiTriageError(null);

        complaintRepository.saveAndFlush(complaint);

        // Step 1: Call the AI classifier
        TriageSuggestion suggestion;

        try {
            suggestion = triageClassifier.classify(
                    complaint.getTitle(),
                    complaint.getDescription(),
                    complaint.getAddress(),
                    allowedDepartmentNames
            );

        } catch (Exception e) {

            log.error(
                    "AI classification failed for complaint ID: {}. Error type: {}",
                    complaintId,
                    e.getClass().getName(),
                    e
            );

            complaint.setAiTriageStatus(AiTriageStatus.FAILED);
            complaint.setAiTriageError(
                    "AI triage failed. Please retry later."
            );

            complaintRepository.save(complaint);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI triage service is temporarily unavailable."
            );
        }

        // Step 2: Apply the successful AI response
        complaint.setAiCategory(suggestion.category());
        complaint.setAiUrgency(suggestion.urgency());
        complaint.setRecommendedDepartment(suggestion.department());
        complaint.setAiReasoning(suggestion.reasoning());
        complaint.setTriagedAt(OffsetDateTime.now());
        complaint.setAiPromptVersion(triageClassifier.getPromptVersion());
        complaint.setAiModel(aiModel);
        complaint.setAiSummary(suggestion.summary());
        complaint.setAiExtractedDetails(suggestion.extractedDetails());
        complaint.setAiMissingInformation(suggestion.missingInformation());

        complaint.setAiTriageStatus(AiTriageStatus.COMPLETED);
        complaint.setAiTriageError(null);

        // Step 3: Persist the successful result
        Complaint saved = complaintRepository.save(complaint);

        log.info(
                "AI triage completed successfully for complaint ID: {}",
                complaintId
        );

        return ComplaintResponse.from(saved);
    }
}
