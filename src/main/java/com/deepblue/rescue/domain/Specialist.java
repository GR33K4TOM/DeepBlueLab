package com.deepblue.rescue.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "specialists")
public class Specialist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String professionalCode;
	private String firstName;
	private String lastName;

	@Column(unique = true)
	private String email;

	private boolean active;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "specialist_expertise",
		joinColumns = @JoinColumn(name = "specialist_id"),
		inverseJoinColumns = @JoinColumn(name = "expertise_id")
	)
	private Set<Expertise> expertiseAreas = new HashSet<>();

	@OneToMany(mappedBy = "specialist")
	private List<Treatment> treatments = new ArrayList<>();

	protected Specialist() {
	}

	public Specialist(String professionalCode, String firstName, String lastName, String email, boolean active) {
		this.professionalCode = professionalCode;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.active = active;
	}

	public void addExpertise(Expertise expertise) {
		if (expertiseAreas.add(expertise)) {
			expertise.getSpecialists().add(this);
		}
	}

	public void addTreatment(Treatment treatment) {
		if (!treatments.contains(treatment)) {
			treatments.add(treatment);
		}
		if (treatment.getSpecialist() != this) {
			treatment.setSpecialist(this);
		}
	}

	public Long getId() { return id; }
	public String getProfessionalCode() { return professionalCode; }
	public void setProfessionalCode(String professionalCode) { this.professionalCode = professionalCode; }
	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public Set<Expertise> getExpertiseAreas() { return expertiseAreas; }
	public List<Treatment> getTreatments() { return treatments; }
}
