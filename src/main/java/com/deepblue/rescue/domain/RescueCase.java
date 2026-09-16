package com.deepblue.rescue.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "rescue_cases")
public class RescueCase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String caseCode;
	private LocalDate rescueDate;
	private String rescueLocation;

	@Enumerated(EnumType.STRING)
	private RescueStatus status;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "rescue_center_id", nullable = false)
	private RescueCenter rescueCenter;

	@OneToOne(mappedBy = "rescueCase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Animal animal;

	protected RescueCase() {
	}

	public RescueCase(String caseCode, LocalDate rescueDate, String rescueLocation, RescueStatus status) {
		this.caseCode = caseCode;
		this.rescueDate = rescueDate;
		this.rescueLocation = rescueLocation;
		this.status = status;
	}

	public void assignAnimal(Animal animal) {
		this.animal = animal;
		if (animal != null && animal.getRescueCase() != this) {
			animal.setRescueCase(this);
		}
	}

	public Long getId() { return id; }
	public String getCaseCode() { return caseCode; }
	public void setCaseCode(String caseCode) { this.caseCode = caseCode; }
	public LocalDate getRescueDate() { return rescueDate; }
	public void setRescueDate(LocalDate rescueDate) { this.rescueDate = rescueDate; }
	public String getRescueLocation() { return rescueLocation; }
	public void setRescueLocation(String rescueLocation) { this.rescueLocation = rescueLocation; }
	public RescueStatus getStatus() { return status; }
	public void setStatus(RescueStatus status) { this.status = status; }
	public RescueCenter getRescueCenter() { return rescueCenter; }
	public void setRescueCenter(RescueCenter rescueCenter) {
		this.rescueCenter = rescueCenter;
		if (rescueCenter != null && !rescueCenter.getRescueCases().contains(this)) {
			rescueCenter.getRescueCases().add(this);
		}
	}
	public Animal getAnimal() { return animal; }
}
