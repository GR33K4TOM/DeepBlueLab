package com.deepblue.rescue.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rescue_centers")
public class RescueCenter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String code;
	private String name;
	private String city;

	@OneToMany(mappedBy = "rescueCenter", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RescueCase> rescueCases = new ArrayList<>();

	protected RescueCenter() {
	}

	public RescueCenter(String code, String name, String city) {
		this.code = code;
		this.name = name;
		this.city = city;
	}

	public void addCase(RescueCase rescueCase) {
		if (!rescueCases.contains(rescueCase)) {
			rescueCases.add(rescueCase);
		}
		if (rescueCase.getRescueCenter() != this) {
			rescueCase.setRescueCenter(this);
		}
	}

	public Long getId() { return id; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getCity() { return city; }
	public void setCity(String city) { this.city = city; }
	public List<RescueCase> getRescueCases() { return rescueCases; }
}
