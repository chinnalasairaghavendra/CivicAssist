
package com.civicassist.triage;

import com.civicassist.complaint.Complaint;
import com.civicassist.complaint.ComplaintRepository;
import com.civicassist.complaint.ComplaintStatus;
import com.civicassist.complaint.dto.ComplaintResponse;
import com.civicassist.department.Department;
import com.civicassist.department.DepartmentRepository;
import com.civicassist.triage.dto.TriageSuggestion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
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
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TriageValidator triageValidator;

    @Mock
    private Complaint complaint;

    @Mock
    private Department department;

    private TriageService triageService;

    private final Long complaintId = 1L;
    private final String authenticatedEmail = "citizen@example.com";

    private final TriageSuggestion suggestion = new TriageSuggestion(
            "ROADS",
            "HIGH",
            "Roads and Infrastructure",
            "The reported pothole may create a road safety risk."
    );

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);

        triageService = new TriageService(
                chatClientBuilder,
                complaintRepository,
                departmentRepository,
                triageValidator
        );
    }

    private void mockComplaintLookup() {
        when(complaintRepository.findByIdAndUser_Email(
                complaintId,
                authenticatedEmail
        )).thenReturn(Optional.of(complaint));
    }

    private void mockActiveDepartments() {
        when(department.getName()).thenReturn("Roads and Infrastructure");

        when(departmentRepository.findAllByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(department));
    }

    private void mockGeminiResponse() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        when(callResponseSpec.entity(TriageSuggestion.class))
                .thenReturn(suggestion);
    }

    private void mockComplaintResponseFields() {
        when(complaint.getId()).thenReturn(complaintId);
        when(complaint.getTitle()).thenReturn("Large pothole near school");
        when(complaint.getDescription()).thenReturn("A large pothole was reported.");
        when(complaint.getCategory()).thenReturn("ROADS");
        when(complaint.getAddress()).thenReturn("Main Road, Hyderabad");
        when(complaint.getStatus()).thenReturn(ComplaintStatus.SUBMITTED);
        when(complaint.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(complaint.getUpdatedAt()).thenReturn(OffsetDateTime.now());
        when(complaint.getAiCategory()).thenReturn("ROADS");
        when(complaint.getAiUrgency()).thenReturn("HIGH");
        when(complaint.getRecommendedDepartment())
                .thenReturn("Roads and Infrastructure");
        when(complaint.getAiReasoning())
                .thenReturn("The reported pothole may create a road safety risk.");
        when(complaint.getTriagedAt()).thenReturn(OffsetDateTime.now());
    }

    private void prepareSuccessfulTriage() {
        mockComplaintLookup();
        mockActiveDepartments();
        mockGeminiResponse();

        when(triageValidator.validate(
                eq(suggestion),
                anySet()
        )).thenReturn(suggestion);

        when(complaintRepository.save(complaint)).thenReturn(complaint);

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

        verify(complaintRepository).findByIdAndUser_Email(
                complaintId,
                authenticatedEmail
        );

        verify(triageValidator).validate(
                eq(suggestion),
                anySet()
        );

        verify(complaint).setAiCategory("ROADS");
        verify(complaint).setAiUrgency("HIGH");
        verify(complaint).setRecommendedDepartment(
                "Roads and Infrastructure"
        );
        verify(complaint).setAiReasoning(suggestion.reasoning());
        verify(complaint).setTriagedAt(any(OffsetDateTime.class));

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

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verifyNoInteractions(departmentRepository);
        verifyNoInteractions(chatClient);
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

        verifyNoInteractions(chatClient);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void shouldPassGeminiSuggestionToValidator() {

        prepareSuccessfulTriage();

        triageService.triage(complaintId, authenticatedEmail);

        verify(triageValidator).validate(
                eq(suggestion),
                argThat(names ->
                        names.equals(Set.of("Roads and Infrastructure"))
                )
        );
    }

    @Test
    void shouldNotSaveComplaintWhenValidatorRejectsSuggestion() {

        mockComplaintLookup();
        mockActiveDepartments();
        mockGeminiResponse();

        when(triageValidator.validate(
                eq(suggestion),
                anySet()
        )).thenThrow(
                new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Invalid AI classification"
                )
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> triageService.triage(
                        complaintId,
                        authenticatedEmail
                )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());

        verify(complaintRepository, never()).save(any());
    }

    @Test
    void shouldThrow502WhenGeminiCallFails() {

        mockComplaintLookup();
        mockActiveDepartments();

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(
                new RuntimeException("Gemini API unavailable")
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> triageService.triage(
                        complaintId,
                        authenticatedEmail
                )
        );

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());

        verifyNoInteractions(triageValidator);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void shouldNotSaveComplaintWhenGeminiReturnsNull() {

        mockComplaintLookup();
        mockActiveDepartments();

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        when(callResponseSpec.entity(TriageSuggestion.class))
                .thenReturn(null);

        when(triageValidator.validate(null, Set.of("Roads and Infrastructure")))
                .thenThrow(
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

        verify(complaintRepository, never()).save(any());
    }
}
