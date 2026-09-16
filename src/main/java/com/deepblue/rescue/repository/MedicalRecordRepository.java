package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.MedicalRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

	Optional<MedicalRecord> findByAnimalId(Long animalId);
}
