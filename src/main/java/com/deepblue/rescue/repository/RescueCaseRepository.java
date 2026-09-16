package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RescueCaseRepository extends JpaRepository<RescueCase, Long> {

	Optional<RescueCase> findByCaseCode(String caseCode);

	@Query("""
		select rescueCase
		from RescueCase rescueCase
		join fetch rescueCase.animal
		where rescueCase.caseCode = :caseCode
		""")
	Optional<RescueCase> findByCaseCodeWithAnimal(@Param("caseCode") String caseCode);

	List<RescueCase> findByStatusOrderByRescueDateAsc(RescueStatus status);

	List<RescueCase> findByRescueCenterCode(String centerCode);

	List<RescueCase> findByRescueDateAfterOrderByRescueDateDesc(LocalDate date);
}
