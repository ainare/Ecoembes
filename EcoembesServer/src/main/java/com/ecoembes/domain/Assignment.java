package com.ecoembes.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignments")
public class Assignment {

	@Id
	private String assignmentId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "plant_id", nullable = false)
	private Plant plant;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	private LocalDate date;

	private int totalPackagingContainers;

	@OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<AssignmentItem> items = new ArrayList<>();

	protected Assignment() {
	}

	public Assignment(String assignmentId, Plant plant, Employee employee, LocalDate date, List<Dumpster> dumpsters,
			int totalPackagingContainers) {

		this.assignmentId = assignmentId;
		this.plant = plant;
		this.employee = employee;
		this.date = date;
		this.totalPackagingContainers = totalPackagingContainers;

		for (Dumpster dumpster : dumpsters) {
			items.add(new AssignmentItem(this, dumpster));
		}
	}

	public String assignmentId() {
		return assignmentId;
	}

	public Plant plant() {
		return plant;
	}

	public Employee employee() {
		return employee;
	}

	public LocalDate date() {
		return date;
	}

	public List<Dumpster> dumpsters() {
		return items.stream().map(AssignmentItem::getDumpster).toList();
	}

	public int totalPackagingContainers() {
		return totalPackagingContainers;
	}

	public int totalDumpsters() {
		return items.size();
	}
}
