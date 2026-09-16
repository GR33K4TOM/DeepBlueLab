package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Specialist;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {

	Optional<Specialist> findByProfessionalCode(
			String professionalCode);

	@Query("""
				select distinct specialist
				from Specialist specialist
				join specialist.expertiseAreas expertise
				where specialist.active = true
					and lower(expertise.name) = lower(:expertiseName)
				order by specialist.lastName, specialist.firstName
				""")
		List<Specialist> findActiveByExpertise(@Param("expertiseName") String expertiseName);
}
