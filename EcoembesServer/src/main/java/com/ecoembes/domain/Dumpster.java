package com.ecoembes.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "dumpsters")
public class Dumpster {

	@Id
	private String id;

	private String address;
	private String postalCode;
	private Integer initialCapacity;

	@Transient
	private NavigableMap<LocalDate, DumpsterReading> readings = new ConcurrentSkipListMap<>();

	protected Dumpster() {
	}

	public Dumpster(String id, String address, String postalCode, Integer initialCapacity) {
		this.id = id;
		this.address = address;
		this.postalCode = postalCode;
		this.initialCapacity = initialCapacity;
	}

	public String id() {
		return id;
	}

	public String address() {
		return address;
	}

	public String postalCode() {
		return postalCode;
	}

	public Integer initialCapacity() {
		return initialCapacity;
	}

	public void saveReading(DumpsterReading reading) {
		readings.put(reading.date(), reading);
	}

	public Optional<DumpsterReading> findReading(LocalDate date) {
		return Optional.ofNullable(readings.get(date));
	}

	public List<DumpsterReading> findReadings(LocalDate startDate, LocalDate endDate) {
		return List.copyOf(readings.subMap(startDate, true, endDate, true).values());
	}
}
