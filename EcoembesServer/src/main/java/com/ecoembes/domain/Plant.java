package com.ecoembes.domain;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "plants")
public class Plant {

	@Id
	private String id;

	private String name;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "plant_capacities", joinColumns = @JoinColumn(name = "plant_id"))
	@MapKeyColumn(name = "capacity_date")
	@Column(name = "available_capacity")
	private Map<LocalDate, Double> availableCapacity = new HashMap<>();

	protected Plant() {
	}

	public Plant(String id, String name, Map<LocalDate, Double> availableCapacity) {

		this.id = id;
		this.name = name;
		this.availableCapacity = new HashMap<>(availableCapacity);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Double getAvailableCapacity(LocalDate date) {
		return availableCapacity.get(date);
	}

	public void setAvailableCapacity(LocalDate date, Double capacity) {
		availableCapacity.put(date, capacity);
	}

}
