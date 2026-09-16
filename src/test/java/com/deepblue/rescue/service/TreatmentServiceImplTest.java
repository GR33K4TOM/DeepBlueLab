package com.deepblue.rescue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.impl.TreatmentServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentMapper mapper;

    @InjectMocks
    private TreatmentServiceImpl service;

    @Test
    void shouldRegisterTreatmentWhenValid() {
        Animal animal = new Animal("AN-001", "Common", "Scientific", AnimalSex.FEMALE);
        RescueCase rescueCase = new RescueCase("RES-001", LocalDate.now().minusDays(5), "Location", RescueStatus.IN_REHABILITATION);
        animal.setRescueCase(rescueCase);
        
        Specialist specialist = new Specialist("SPEC-001", "F", "L", "email@e.com", true);
        
        CreateTreatmentRequest request = new CreateTreatmentRequest("AN-001", "SPEC-001", LocalDateTime.now(), TreatmentType.WOUND_CARE, "Description");
        TreatmentResponse response = new TreatmentResponse(1L, "AN-001", "SPEC-001", LocalDateTime.now(), TreatmentType.WOUND_CARE, "Description");

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));
        when(treatmentRepository.save(any(Treatment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toResponse(any(Treatment.class))).thenReturn(response);

        TreatmentResponse result = service.register(request);

        assertThat(result).isEqualTo(response);
        verify(treatmentRepository).save(any(Treatment.class));
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenSpecialistIsInactive() {
        Animal animal = new Animal("AN-001", "Common", "Scientific", AnimalSex.FEMALE);
        Specialist specialist = new Specialist("SPEC-001", "F", "L", "email@e.com", false); // Inactive
        
        CreateTreatmentRequest request = new CreateTreatmentRequest("AN-001", "SPEC-001", LocalDateTime.now(), TreatmentType.WOUND_CARE, "Description");

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenCaseIsReleased() {
        Animal animal = new Animal("AN-001", "Common", "Scientific", AnimalSex.FEMALE);
        RescueCase rescueCase = new RescueCase("RES-001", LocalDate.now().minusDays(5), "Location", RescueStatus.RELEASED);
        animal.setRescueCase(rescueCase);
        
        Specialist specialist = new Specialist("SPEC-001", "F", "L", "email@e.com", true);
        
        CreateTreatmentRequest request = new CreateTreatmentRequest("AN-001", "SPEC-001", LocalDateTime.now(), TreatmentType.OBSERVATION, "Description");

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenTreatmentDateIsBeforeRescueDate() {
        Animal animal = new Animal("AN-001", "Common", "Scientific", AnimalSex.FEMALE);
        RescueCase rescueCase = new RescueCase("RES-001", LocalDate.now().minusDays(5), "Location", RescueStatus.IN_REHABILITATION);
        animal.setRescueCase(rescueCase);
        
        Specialist specialist = new Specialist("SPEC-001", "F", "L", "email@e.com", true);
        
        CreateTreatmentRequest request = new CreateTreatmentRequest("AN-001", "SPEC-001", LocalDateTime.now().minusDays(10), TreatmentType.WOUND_CARE, "Description");

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001")).thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }
}
