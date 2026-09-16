package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

		Optional<Animal> findByAnimalCode(String animalCode);

		List<Animal> findByCommonNameContainingIgnoreCase(String commonName);

		List<Animal> findByRescueCaseStatus(RescueStatus status);

		List<Animal> findByRescueCaseRescueCenterCode(String centerCode);

		@Query("""
				select distinct a
				from Animal a
				join a.rescueCase rescueCase
				join a.treatments treatment
				join treatment.specialist specialist
				join specialist.expertiseAreas expertise
				where rescueCase.status = :status
					and lower(expertise.name) = lower(:expertiseName)
				""")
		List<Animal> findByStatusAndTreatmentSpecialistExpertise(
						@Param("status") RescueStatus status,
						@Param("expertiseName") String expertiseName);
}
