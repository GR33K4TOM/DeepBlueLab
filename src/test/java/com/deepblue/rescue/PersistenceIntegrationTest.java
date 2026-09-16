package com.deepblue.rescue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.Expertise;
import com.deepblue.rescue.domain.MedicalRecord;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueCenter;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.ExpertiseRepository;
import com.deepblue.rescue.repository.MedicalRecordRepository;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.repository.RescueCenterRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PersistenceIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
			.withDatabaseName("deepblue_test")
			.withUsername("deepblue")
			.withPassword("deepblue");

	private final RescueCenterRepository rescueCenterRepository;
	private final RescueCaseRepository rescueCaseRepository;
	private final AnimalRepository animalRepository;
	private final MedicalRecordRepository medicalRecordRepository;
	private final SpecialistRepository specialistRepository;
	private final ExpertiseRepository expertiseRepository;
	private final TreatmentRepository treatmentRepository;
	private final JdbcTemplate jdbcTemplate;

	PersistenceIntegrationTest(
			RescueCenterRepository rescueCenterRepository,
			RescueCaseRepository rescueCaseRepository,
			AnimalRepository animalRepository,
			MedicalRecordRepository medicalRecordRepository,
			SpecialistRepository specialistRepository,
			ExpertiseRepository expertiseRepository,
			TreatmentRepository treatmentRepository,
			JdbcTemplate jdbcTemplate) {
		this.rescueCenterRepository = rescueCenterRepository;
		this.rescueCaseRepository = rescueCaseRepository;
		this.animalRepository = animalRepository;
		this.medicalRecordRepository = medicalRecordRepository;
		this.specialistRepository = specialistRepository;
		this.expertiseRepository = expertiseRepository;
		this.treatmentRepository = treatmentRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void flywayExecutesAllMigrations() {
		List<String> versions = jdbcTemplate.queryForList(
				"select version from flyway_schema_history order by installed_rank",
				String.class);

		assertThat(versions).contains("1", "2", "3");
	}

	@Test
	void inheritedRepositoryMethodsWork() {
		RescueCenter center = rescueCenterRepository.save(new RescueCenter("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta"));

		assertThat(center.getId()).isNotNull();
		assertThat(rescueCenterRepository.findById(center.getId())).contains(center);
		assertThat(rescueCenterRepository.existsById(center.getId())).isTrue();
		assertThat(rescueCenterRepository.count()).isEqualTo(1);
	}

	@Test
	void persistsOneToManyAndOneToOneRelationships() {
		RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
		RescueCase firstCase = createCase("RES-001", LocalDate.of(2026, 8, 1), RescueStatus.IN_REHABILITATION, "Turtle Bay");
		RescueCase secondCase = createCase("RES-002", LocalDate.of(2026, 8, 2), RescueStatus.READY_FOR_RELEASE, "Coral Point");
		center.addCase(firstCase);
		center.addCase(secondCase);

		rescueCenterRepository.saveAndFlush(center);

		assertThat(rescueCaseRepository.findByRescueCenterCode("DB-CAR")).hasSize(2);
		assertThat(firstCase.getAnimal().getRescueCase()).isSameAs(firstCase);
		assertThat(firstCase.getAnimal().getId()).isNotNull();
	}

	@Test
	void cascadesMedicalRecordThroughAnimal() {
		RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
		RescueCase rescueCase = createCase("RES-003", LocalDate.of(2026, 8, 3), RescueStatus.ADMITTED, "Harbor");
		MedicalRecord record = new MedicalRecord(
				new BigDecimal("28.40"),
				"STABLE",
				"Left front flipper injury",
				"Observation required");
		rescueCase.getAnimal().assignMedicalRecord(record);
		center.addCase(rescueCase);

		rescueCenterRepository.saveAndFlush(center);

		assertThat(record.getId()).isNotNull();
		assertThat(medicalRecordRepository.findByAnimalId(rescueCase.getAnimal().getId())).contains(record);
	}

	@Test
	void persistsManyToManyExpertise() {
		Expertise trauma = expertiseRepository.findByNameIgnoreCase("trauma").orElseThrow();
		Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow();
		Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@deepblue.org", true);
		specialist.addExpertise(trauma);
		specialist.addExpertise(rehabilitation);

		specialistRepository.saveAndFlush(specialist);

		assertThat(specialistRepository.findById(specialist.getId()).orElseThrow().getExpertiseAreas())
				.hasSize(2);
	}

	@Test
	void queryMethodsNavigateRelationsAndOrderResults() {
		RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
		RescueCase admitted = createCase("RES-001", LocalDate.of(2026, 8, 1), RescueStatus.ADMITTED, "A");
		RescueCase rehabilitation = createCase("RES-002", LocalDate.of(2026, 8, 20), RescueStatus.IN_REHABILITATION, "B");
		center.addCase(admitted);
		center.addCase(rehabilitation);
		rescueCenterRepository.saveAndFlush(center);

		assertThat(rescueCaseRepository.findByStatusOrderByRescueDateAsc(RescueStatus.IN_REHABILITATION))
				.extracting(RescueCase::getCaseCode).containsExactly("RES-002");
		assertThat(animalRepository.findByRescueCaseStatus(RescueStatus.ADMITTED))
				.extracting(Animal::getAnimalCode).containsExactly("AN-RES-001");
		assertThat(animalRepository.findByRescueCaseRescueCenterCode("DB-CAR")).hasSize(2);
		assertThat(animalRepository.findByCommonNameContainingIgnoreCase("turtle")).hasSize(2);
		assertThat(rescueCaseRepository.findByRescueDateAfterOrderByRescueDateDesc(LocalDate.of(2026, 8, 5)))
				.extracting(RescueCase::getCaseCode).containsExactly("RES-002");
	}

	@Test
	void joinFetchLoadsCaseAnimalAndFindsCaseByCode() {
		RescueCenter center = new RescueCenter("DB-FETCH", "DeepBlue Fetch", "Santa Marta");
		RescueCase rescueCase = createCase("RES-FETCH", LocalDate.of(2026, 8, 12), RescueStatus.ADMITTED, "Bay");
		center.addCase(rescueCase);
		rescueCenterRepository.saveAndFlush(center);

		assertThat(rescueCaseRepository.findByCaseCodeWithAnimal("RES-FETCH"))
				.get().extracting(RescueCase::getAnimal).isNotNull();
	}

	@Test
	void jpqlFindsActiveSpecialistsAndTreatments() {
		RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
		RescueCase rescueCase = createCase("RES-010", LocalDate.of(2026, 8, 10), RescueStatus.IN_REHABILITATION, "Bay");
		center.addCase(rescueCase);
		rescueCenterRepository.saveAndFlush(center);

		Specialist elena = specialist("SPEC-010", "Elena", "Vargas", "elena10@deepblue.org", true, "Trauma");
		Specialist mateo = specialist("SPEC-011", "Mateo", "Rojas", "mateo11@deepblue.org", true, "Rehabilitation");
		specialistRepository.saveAllAndFlush(List.of(elena, mateo));

		Treatment first = treatment(rescueCase.getAnimal(), elena, LocalDateTime.of(2026, 8, 10, 10, 0), TreatmentType.WOUND_CARE);
		Treatment second = treatment(rescueCase.getAnimal(), mateo, LocalDateTime.of(2026, 8, 20, 10, 0), TreatmentType.OBSERVATION);
		treatmentRepository.saveAllAndFlush(List.of(second, first));

		assertThat(specialistRepository.findActiveByExpertise("trauma"))
				.extracting(Specialist::getProfessionalCode).containsExactly("SPEC-010");
		assertThat(treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(rescueCase.getAnimal().getId()))
				.extracting(Treatment::getType).containsExactly(TreatmentType.WOUND_CARE, TreatmentType.OBSERVATION);
		assertThat(treatmentRepository.findPerformedBetween(
				LocalDateTime.of(2026, 8, 15, 0, 0), LocalDateTime.of(2026, 8, 25, 0, 0)))
				.extracting(Treatment::getType).containsExactly(TreatmentType.OBSERVATION);
		assertThat(treatmentRepository.findByRescueCenterCode("DB-CAR")).hasSize(2);
		assertThat(treatmentRepository.findBySpecialistExpertise("rehabilitation")).hasSize(1);
	}

	@Test
	void challengeFindsRehabilitationAnimalsTreatedByTraumaExperts() {
		RescueCenter center = new RescueCenter("DB-CAR", "DeepBlue Caribbean", "Santa Marta");
		RescueCase rescueCase = createCase("RES-100", LocalDate.of(2026, 8, 18), RescueStatus.IN_REHABILITATION, "Bahia Concha");
		center.addCase(rescueCase);
		rescueCenterRepository.saveAndFlush(center);

		Specialist specialist = specialist("SPEC-100", "Elena", "Vargas", "elena100@deepblue.org", true, "Trauma");
		specialistRepository.saveAndFlush(specialist);
		treatmentRepository.saveAndFlush(treatment(
				rescueCase.getAnimal(), specialist, LocalDateTime.of(2026, 8, 18, 10, 0), TreatmentType.WOUND_CARE));

		assertThat(animalRepository.findByStatusAndTreatmentSpecialistExpertise(
				RescueStatus.IN_REHABILITATION, "trauma"))
				.extracting(Animal::getAnimalCode).containsExactly("AN-RES-100");
	}

	@Test
	void databaseUniqueConstraintRejectsDuplicateCenterCode() {
		rescueCenterRepository.saveAndFlush(new RescueCenter("DB-DUP", "One", "City"));

		assertThatThrownBy(() -> rescueCenterRepository.saveAndFlush(new RescueCenter("DB-DUP", "Two", "City")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void databaseUniqueConstraintRejectsDuplicateAnimalCode() {
		RescueCenter center = new RescueCenter("DB-ANIMAL-DUP", "DeepBlue Animals", "Santa Marta");
		RescueCase firstCase = createCase("RES-ANIMAL-DUP-1", LocalDate.of(2026, 8, 1), RescueStatus.ADMITTED, "Bay");
		RescueCase secondCase = createCase("RES-ANIMAL-DUP-2", LocalDate.of(2026, 8, 2), RescueStatus.ADMITTED, "Bay");
		secondCase.getAnimal().setAnimalCode(firstCase.getAnimal().getAnimalCode());
		center.addCase(firstCase);
		center.addCase(secondCase);

		assertThatThrownBy(() -> rescueCenterRepository.saveAndFlush(center))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void databaseCheckConstraintRejectsInvalidStatus() {
		RescueCenter center = rescueCenterRepository.saveAndFlush(new RescueCenter("DB-CHECK", "Check Center", "City"));

		assertThatThrownBy(() -> jdbcTemplate.update(
				"insert into rescue_cases (case_code, rescue_date, rescue_location, status, rescue_center_id) values (?, ?, ?, ?, ?)",
				"BAD-STATUS", LocalDate.now(), "Nowhere", "INVALID", center.getId()))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void databaseForeignKeyRejectsUnknownRescueCenter() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"insert into rescue_cases (case_code, rescue_date, rescue_location, status, rescue_center_id) values (?, ?, ?, ?, ?)",
				"BAD-FK", LocalDate.now(), "Nowhere", "ADMITTED", -1L))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void databaseUniqueConstraintRejectsDuplicateTrackingDevice() {
		RescueCenter center = new RescueCenter("DB-GPS", "DeepBlue GPS", "Santa Marta");
		RescueCase firstCase = createCase("RES-GPS-1", LocalDate.of(2026, 8, 1), RescueStatus.ADMITTED, "Bay");
		firstCase.getAnimal().setTrackingDeviceCode("GPS-001");
		center.addCase(firstCase);
		rescueCenterRepository.saveAndFlush(center);

		RescueCenter secondCenter = new RescueCenter("DB-GPS-2", "DeepBlue GPS 2", "Santa Marta");
		RescueCase secondCase = createCase("RES-GPS-2", LocalDate.of(2026, 8, 2), RescueStatus.ADMITTED, "Bay");
		secondCase.getAnimal().setTrackingDeviceCode("GPS-001");
		secondCenter.addCase(secondCase);

		assertThatThrownBy(() -> rescueCenterRepository.saveAndFlush(secondCenter))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void persistsTheCompleteIntegrationScenario() {
		RescueCenter center = new RescueCenter("DB-CAR-100", "DeepBlue Caribbean", "Santa Marta");
		RescueCase rescueCase = new RescueCase(
				"RES-2026-100", LocalDate.of(2026, 8, 18), "Bahia Concha", RescueStatus.IN_REHABILITATION);
		Animal animal = new Animal("AN-2026-100", "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE);
		animal.assignMedicalRecord(new MedicalRecord(
				new BigDecimal("27.80"), "STABLE", "Injury caused by fishing net", "Possible plastic ingestion"));
		rescueCase.assignAnimal(animal);
		center.addCase(rescueCase);
		rescueCenterRepository.saveAndFlush(center);

		Specialist specialist = new Specialist("SPEC-001", "Elena", "Vargas", "elena@deepblue.org", true);
		specialist.addExpertise(expertiseRepository.findByNameIgnoreCase("Marine Reptiles").orElseThrow());
		specialist.addExpertise(expertiseRepository.findByNameIgnoreCase("Trauma").orElseThrow());
		specialist.addExpertise(expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow());
		specialistRepository.saveAndFlush(specialist);

		Treatment woundCare = treatment(animal, specialist, LocalDateTime.of(2026, 8, 18, 10, 0), TreatmentType.WOUND_CARE);
		woundCare.setDescription("Cleaning of left front flipper");
		Treatment hydration = treatment(animal, specialist, LocalDateTime.of(2026, 8, 19, 10, 0), TreatmentType.HYDRATION);
		hydration.setDescription("Subcutaneous fluid therapy");
		treatmentRepository.saveAllAndFlush(List.of(woundCare, hydration));

		assertThat(rescueCaseRepository.findByCaseCode("RES-2026-100")).isPresent();
		assertThat(animalRepository.findByRescueCaseRescueCenterCode("DB-CAR-100"))
				.extracting(Animal::getCommonName).containsExactly("Green Sea Turtle");
		assertThat(animalRepository.findByCommonNameContainingIgnoreCase("turtle")).hasSize(1);
		assertThat(specialistRepository.findActiveByExpertise("Trauma")).containsExactly(specialist);
		assertThat(treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(animal.getId()))
				.extracting(Treatment::getType).containsExactly(TreatmentType.WOUND_CARE, TreatmentType.HYDRATION);
		assertThat(treatmentRepository.findBySpecialistExpertise("Rehabilitation")).hasSize(2);
	}

	private RescueCase createCase(String caseCode, LocalDate rescueDate, RescueStatus status, String location) {
		RescueCase rescueCase = new RescueCase(caseCode, rescueDate, location, status);
		rescueCase.assignAnimal(new Animal("AN-" + caseCode, "Green Sea Turtle", "Chelonia mydas", AnimalSex.FEMALE));
		return rescueCase;
	}

	private Specialist specialist(
			String code, String firstName, String lastName, String email, boolean active, String expertiseName) {
		Specialist specialist = new Specialist(code, firstName, lastName, email, active);
		specialist.addExpertise(expertiseRepository.findByNameIgnoreCase(expertiseName).orElseThrow());
		return specialist;
	}

	private Treatment treatment(Animal animal, Specialist specialist, LocalDateTime performedAt, TreatmentType type) {
		Treatment treatment = new Treatment(performedAt, type, type.name());
		animal.addTreatment(treatment);
		specialist.addTreatment(treatment);
		return treatment;
	}
}
