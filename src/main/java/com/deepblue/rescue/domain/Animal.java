package com.deepblue.rescue.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "animals")
public class Animal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String animalCode;
	private String commonName;
	private String scientificName;

	@Enumerated(EnumType.STRING)
	private AnimalSex sex;

	@Column(name = "tracking_device_code")
	private String trackingDeviceCode;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "rescue_case_id", nullable = false, unique = true)
	private RescueCase rescueCase;

	@OneToOne(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private MedicalRecord medicalRecord;

	@OneToMany(mappedBy = "animal")
	private List<Treatment> treatments = new ArrayList<>();

	protected Animal() {
	}

	public Animal(String animalCode, String commonName, String scientificName, AnimalSex sex) {
		this.animalCode = animalCode;
		this.commonName = commonName;
		this.scientificName = scientificName;
		this.sex = sex;
	}

	public void assignMedicalRecord(MedicalRecord medicalRecord) {
		this.medicalRecord = medicalRecord;
		if (medicalRecord != null && medicalRecord.getAnimal() != this) {
			medicalRecord.setAnimal(this);
		}
	}

	public void addTreatment(Treatment treatment) {
		if (!treatments.contains(treatment)) {
			treatments.add(treatment);
		}
		if (treatment.getAnimal() != this) {
			treatment.setAnimal(this);
		}
	}

	public Long getId() { return id; }
	public String getAnimalCode() { return animalCode; }
	public void setAnimalCode(String animalCode) { this.animalCode = animalCode; }
	public String getCommonName() { return commonName; }
	public void setCommonName(String commonName) { this.commonName = commonName; }
	public String getScientificName() { return scientificName; }
	public void setScientificName(String scientificName) { this.scientificName = scientificName; }
	public AnimalSex getSex() { return sex; }
	public void setSex(AnimalSex sex) { this.sex = sex; }
	public String getTrackingDeviceCode() { return trackingDeviceCode; }
	public void setTrackingDeviceCode(String trackingDeviceCode) { this.trackingDeviceCode = trackingDeviceCode; }
	public RescueCase getRescueCase() { return rescueCase; }
	public void setRescueCase(RescueCase rescueCase) {
		this.rescueCase = rescueCase;
		if (rescueCase != null && rescueCase.getAnimal() != this) {
			rescueCase.assignAnimal(this);
		}
	}
	public MedicalRecord getMedicalRecord() { return medicalRecord; }
	public List<Treatment> getTreatments() { return treatments; }
}
