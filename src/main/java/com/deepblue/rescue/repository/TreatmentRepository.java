package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Treatment;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

	List<Treatment> findByAnimalIdOrderByPerformedAtAsc(Long animalId);
	List<Treatment> findByAnimalAnimalCodeOrderByPerformedAtAsc(String animalCode);

	@Query("""
		select treatment
		from Treatment treatment
		where treatment.performedAt between :start and :end
		order by treatment.performedAt asc
		""")
	List<Treatment> findPerformedBetween(
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	@Query("""
		select treatment
		from Treatment treatment
		join treatment.animal animal
		join animal.rescueCase rescueCase
		join rescueCase.rescueCenter rescueCenter
		where rescueCenter.code = :centerCode
		order by treatment.performedAt asc
		""")
	List<Treatment> findByRescueCenterCode(@Param("centerCode") String centerCode);

	@Query("""
		select distinct treatment
		from Treatment treatment
		join treatment.specialist specialist
		join specialist.expertiseAreas expertise
		where lower(expertise.name) = lower(:expertiseName)
		order by treatment.performedAt asc
		""")
	List<Treatment> findBySpecialistExpertise(@Param("expertiseName") String expertiseName);
}
