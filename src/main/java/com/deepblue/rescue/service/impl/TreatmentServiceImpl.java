package com.deepblue.rescue.service.impl;


import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.TreatmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TreatmentServiceImpl
        implements TreatmentService {

    private final AnimalRepository animalRepository;

    private final SpecialistRepository specialistRepository;

    private final TreatmentRepository treatmentRepository;

    private final TreatmentMapper mapper;

    public TreatmentServiceImpl(
            AnimalRepository animalRepository,
            SpecialistRepository specialistRepository,
            TreatmentRepository treatmentRepository,
            TreatmentMapper mapper) {

        this.animalRepository = animalRepository;
        this.specialistRepository = specialistRepository;
        this.treatmentRepository = treatmentRepository;
        this.mapper = mapper;
    }

    @Override
    public List<TreatmentResponse> findByAnimalCode(String animalCode) {
        return treatmentRepository.findByAnimalAnimalCodeOrderByPerformedAtAsc(animalCode)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TreatmentResponse register(CreateTreatmentRequest request) {
        // 1 y 2. Buscar Animal y Specialist
        Animal animal = animalRepository.findByAnimalCode(request.animalCode())
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + request.animalCode()));

        Specialist specialist = specialistRepository.findByProfessionalCode(request.specialistCode())
                .orElseThrow(() -> new ResourceNotFoundException("Specialist not found: " + request.specialistCode()));

        // 3. Validar specialist.active
        if (!specialist.isActive()) {
            throw new BusinessRuleException("Specialist is not active");
        }

        // 4. Obtener RescueCase del Animal (suponiendo que Animal tiene una referencia a su caso)
        RescueCase rescueCase = animal.getRescueCase();

        // 5. Validar status del caso
        if (rescueCase.getStatus() == RescueStatus.RELEASED || rescueCase.getStatus() == RescueStatus.CLOSED) {
            throw new BusinessRuleException("Cannot register treatment for a released or closed case");
        }

        // 6. Validar performedAt (la fecha no puede ser anterior al rescate)
        if (request.performedAt().toLocalDate().isBefore(rescueCase.getRescueDate())) {
            throw new BusinessRuleException("Treatment date cannot be before rescue date");
        }

        // 7. Crear Treatment
        Treatment treatment = new Treatment(
                animal,
                specialist,
                request.performedAt(),
                request.type(),
                request.description()
        );

        // 8. Guardar Treatment
        Treatment savedTreatment = treatmentRepository.save(treatment);

        // 9. Mapear TreatmentResponse
        return mapper.toResponse(savedTreatment);
    }
}
