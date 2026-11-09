
package com.civicassist.triage;

import com.civicassist.complaint.AiTriageStatus;
import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.department.Department;
import com.civicassist.department.DepartmentRepository;
import com.civicassist.triage.dto.ExtractedDetail;
import com.civicassist.triage.dto.TriageSuggestion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriageServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private Complaint complaint;

    @Mock
    private Department department;

    @Mock
    private TriageClassifier triageClassifier;

    private TriageService triageService;

    private final Long complaintId = 1L;
    private final String authenticatedEmail = "citizen@example.com";

    private final TriageSuggestion suggestion = new TriageSuggestion(
            "ROADS",
            "HIGH",
            "Roads and Infrastructure",
            "The reported pothole may create a road safety risk.",
            "A pothole has been reported that may create a road safety risk.",
            List.of(
                    new ExtractedDetail("issue", "Pothole"),
                    new ExtractedDetail("impact", "Road safety risk")
            ),
            List.of("Exact pothole location")
    );

    @BeforeEach
    void setUp() {
        triageService = new TriageService(
                complaintRepository,
                departmentRepository,
                triageClassifier
        );

        ReflectionTestUtils.setField(
                triageService,
                "aiModel",
                "gemini-3.6-flash"
        );
    }

    private void mockComplaintLookup() {
        when(complaintRepository.findByIdAndUser_Email(
                complaintId,
                authenticatedEmail
        )).thenReturn(Optional.of(complaint));
    }
    private void mockComplaintDetails() {
        when(complaint.getTitle())
                .thenReturn("Large pothole near school");

        when(complaint.getDescription())
                .thenReturn("A large pothole was reported.");

        when(complaint.getAddress())
                .thenReturn("Main Road, Hyderabad");
    }

    private void mockActiveDepartments() {

        when(department.getName())
                .thenReturn("Roads and Infrastructure");

        when(departmentRepository.findAllByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(department));
    }

    private void mockComplaintResponseFields() {

        when(complaint.getId()).thenReturn(complaintId);

        when(complaint.getCategory()).thenReturn("ROADS");

        when(complaint.getStatus())
                .thenReturn(ComplaintStatus.SUBMITTED);

        when(complaint.getCreatedAt())
                .thenReturn(OffsetDateTime.now());

        when(complaint.getUpdatedAt())
                .thenReturn(OffsetDateTime.now());

        when(complaint.getAiCategory()).thenReturn("ROADS");

        when(complaint.getAiUrgency()).thenReturn("HIGH");

        when(complaint.getRecommendedDepartment())
                .thenReturn("Roads and Infrastructure");

        when(complaint.getAiReasoning())
                .thenReturn(suggestion.reasoning());

        when(complaint.getAiModel())
                .thenReturn("gemini-3.6-flash");

        when(complaint.getAiPromptVersion())
                .thenReturn("triage-v2");

        when(complaint.getTriagedAt())
                .thenReturn(OffsetDateTime.now());

        // Structured extraction response fields.
        when(complaint.getAiSummary())
                .thenReturn(suggestion.summary());

        when(complaint.getAiExtractedDetails())
                .thenReturn(suggestion.extractedDetails());

        when(complaint.getAiMissingInformation())
                .thenReturn(suggestion.missingInformation());
    }

    private void prepareSuccessfulTriage() {
        mockComplaintLookup();
        mockComplaintDetails();
        mockActiveDepartments();

        when(triageClassifier.getPromptVersion())
                .thenReturn("triage-v2");

        when(triageClassifier.classify(
                anyString(),
                anyString(),
                any(),
                anySet()
        )).thenReturn(suggestion);

        when(complaintRepository.save(complaint))
                .thenReturn(complaint);

        mockComplaintResponseFields();
    }

    @Test
    void shouldSuccessfullyTriageComplaint() {

        prepareSuccessfulTriage();

        ComplaintResponse response = triageService.triage(
                complaintId,
                authenticatedEmail
        );

        assertNotNull(response);

        assertEquals(complaintId, response.id());
        assertEquals("ROADS", response.aiCategory());
        assertEquals("HIGH", response.aiUrgency());

        assertEquals(
                "Roads and Infrastructure",
                response.recommendedDepartment()
        );

        assertEquals("gemini-3.6-flash", response.aiModel());
        assertEquals("triage-v2", response.aiPromptVersion());

        // Verify structured extraction response.
        assertEquals(suggestion.summary(), response.aiSummary());

        assertEquals(
                suggestion.extractedDetails(),
                response.aiExtractedDetails()
        );

        assertEquals(
                suggestion.missingInformation(),
                response.aiMissingInformation()
        );

        verify(complaintRepository).findByIdAndUser_Email(
                complaintId,
                authenticatedEmail
        );

        verify(triageClassifier).classify(
                eq("Large pothole near school"),
                eq("A large pothole was reported."),
                eq("Main Road, Hyderabad"),
                eq(Set.of("Roads and Infrastructure"))
        );

        // Verify classification persistence.
        verify(complaint).setAiCategory("ROADS");
        verify(complaint).setAiUrgency("HIGH");

        verify(complaint).setRecommendedDepartment(
                "Roads and Infrastructure"
        );

        verify(complaint).setAiReasoning(suggestion.reasoning());

        // Verify extraction persistence.
        verify(complaint).setAiSummary(suggestion.summary());

        verify(complaint).setAiExtractedDetails(
                suggestion.extractedDetails()
        );

        verify(complaint).setAiMissingInformation(
                suggestion.missingInformation()
        );

        verify(complaint).setTriagedAt(any(OffsetDateTime.class));

        verify(complaint).setAiPromptVersion("triage-v2");
        verify(complaint).setAiModel("gemini-3.6-flash");

        verify(complaintRepository).save(complaint);
    }

    @Test
    void shouldThrow404WhenComplaintDoesNotExist() {

        when(complaintRepository.findByIdAndUser_Email(
                complaintId,
                authenticatedEmail
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> triageService.triage(
                        complaintId,
                        authenticatedEmail
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verifyNoInteractions(triageClassifier);
        verifyNoInteractions(departmentRepository);

        verify(complaintRepository, never()).save(any());
    }

    @Test
    void shouldThrow503WhenNoActiveDepartmentsExist() {

        mockComplaintLookup();

        when(departmentRepository.findAllByActiveTrueOrderByNameAsc())
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> triageService.triage(
                        complaintId,
                        authenticatedEmail
                )
        );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatusCode()
        );

        verifyNoInteractions(triageClassifier);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void shouldPassComplaintDetailsAndDepartmentsToClassifier() {

        prepareSuccessfulTriage();

        triageService.triage(complaintId, authenticatedEmail);

        verify(triageClassifier).classify(
                eq("Large pothole near school"),
                eq("A large pothole was reported."),
                eq("Main Road, Hyderabad"),
                eq(Set.of("Roads and Infrastructure"))
        );
    }

    @Test
    void shouldThrow502WhenClassifierFails() {
        mockComplaintLookup();
        mockComplaintDetails();
        mockActiveDepartments();
        when(triageClassifier.classify(
                anyString(),
                anyString(),
                any(),
                anySet()
        )).thenThrow(
                new RuntimeException("AI classification failed")
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> triageService.triage(
                        complaintId,
                        authenticatedEmail
                )
        );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                exception.getStatusCode()
        );

        verify(complaintRepository).saveAndFlush(complaint);

        verify(complaintRepository).save(complaint);

        verify(complaint).setAiTriageStatus(AiTriageStatus.PROCESSING);
        verify(complaint).setAiTriageStatus(AiTriageStatus.FAILED);
        verify(complaint).setAiTriageAttempts(1);
        verify(complaint).setAiTriageError(
                "AI triage failed. Please retry later."
        );
    }

    @Test
    void shouldIncludeAiMetadataInComplaintResponse() {

        Complaint complaint = mock(Complaint.class);

        when(complaint.getAiModel())
                .thenReturn("gemini-3.6-flash");

        when(complaint.getAiPromptVersion())
                .thenReturn("triage-v2");

        when(complaint.getAiSummary())
                .thenReturn(suggestion.summary());

        when(complaint.getAiExtractedDetails())
                .thenReturn(suggestion.extractedDetails());

        when(complaint.getAiMissingInformation())
                .thenReturn(suggestion.missingInformation());

        ComplaintResponse response = ComplaintResponse.from(complaint);

        assertEquals("gemini-3.6-flash", response.aiModel());
        assertEquals("triage-v2", response.aiPromptVersion());

        assertEquals(suggestion.summary(), response.aiSummary());

        assertEquals(
                suggestion.extractedDetails(),
                response.aiExtractedDetails()
        );

        assertEquals(
                suggestion.missingInformation(),
                response.aiMissingInformation()
        );
    }

    @Test
    void shouldNotSaveComplaintWhenClassifierReturnsInvalidResponse() {

        mockComplaintLookup();
        mockComplaintDetails();
        mockActiveDepartments();

        when(triageClassifier.classify(
                anyString(),
                anyString(),
                any(),
                anySet()
        )).thenThrow(
                new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid AI classification"
                )
        );

        assertThrows(
                ResponseStatusException.class,
                () -> triageService.triage(
                        complaintId,
                        authenticatedEmail
                )
        );

        verify(complaint, never()).setAiCategory(anyString());
        verify(complaint, never()).setAiUrgency(anyString());
        verify(complaint, never()).setRecommendedDepartment(anyString());
        verify(complaint, never()).setAiReasoning(anyString());

        verify(complaint, never()).setAiSummary(anyString());
        verify(complaint, never()).setAiExtractedDetails(anyList());
        verify(complaint, never()).setAiMissingInformation(anyList());

        verify(complaint, never()).setAiModel(anyString());
        verify(complaint, never()).setAiPromptVersion(anyString());
        verify(complaint, never()).setTriagedAt(any());

        verify(complaintRepository).saveAndFlush(complaint);

        verify(complaintRepository).save(complaint);

        verify(complaint).setAiTriageStatus(AiTriageStatus.FAILED);
    }
}
