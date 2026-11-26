package com.ecoembes.domain;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "dumpster_readings", uniqueConstraints = @UniqueConstraint(columnNames = { "dumpster_id", "date" }))
public class DumpsterReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate date;

	private Integer estimatedContainers;

	@Enumerated(EnumType.STRING)
	private FillLevel fillLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dumpster_id", nullable = false)
	private Dumpster dumpster;

	protected DumpsterReading() {
	}

	public DumpsterReading(LocalDate date, Integer estimatedContainers, FillLevel fillLevel) {
		this.date = date;
		this.estimatedContainers = estimatedContainers;
		this.fillLevel = fillLevel;
	}

	public LocalDate date() {
		return date;
	}

	public Integer estimatedContainers() {
		return estimatedContainers;
	}

	public FillLevel fillLevel() {
		return fillLevel;
	}

	public Dumpster getDumpster() {
		return dumpster;
	}

	public void setDumpster(Dumpster dumpster) {
		this.dumpster = dumpster;
	}
}
