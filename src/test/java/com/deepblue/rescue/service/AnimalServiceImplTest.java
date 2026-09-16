package com.deepblue.rescue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.impl.AnimalServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.deepblue.rescue.mapper.AnimalMapper;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository repository;

    @Mock
    private AnimalMapper mapper;

    @InjectMocks
    private AnimalServiceImpl service;

    @Test
    void canReceiveTreatmentReturnsTrueWhenInRehabilitation() {
        Animal animal = new Animal("AN-001", "Common", "Scientific", AnimalSex.MALE);
        RescueCase rescueCase = new RescueCase("RES-001", LocalDate.now(), "Location", RescueStatus.IN_REHABILITATION);
        animal.setRescueCase(rescueCase);

        when(repository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));

        boolean canReceive = service.canReceiveTreatment("AN-001");

        assertThat(canReceive).isTrue();
    }

    @Test
    void canReceiveTreatmentReturnsFalseWhenReleased() {
        Animal animal = new Animal("AN-001", "Common", "Scientific", AnimalSex.MALE);
        RescueCase rescueCase = new RescueCase("RES-001", LocalDate.now(), "Location", RescueStatus.RELEASED);
        animal.setRescueCase(rescueCase);

        when(repository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));

        boolean canReceive = service.canReceiveTreatment("AN-001");

        assertThat(canReceive).isFalse();
    }
}
