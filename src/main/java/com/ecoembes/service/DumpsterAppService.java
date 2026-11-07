package com.ecoembes.service;

import com.ecoembes.dto.DumpsterUsageDTO;
import com.ecoembes.dto.DumpsterStatusDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dto.CreateDumpsterDTO;
import com.ecoembes.dto.DumpsterDTO;
import com.ecoembes.dto.DumpsterReadingDTO;
import com.ecoembes.dto.UpdateDumpsterDTO;
import com.ecoembes.dto.UpdateDumpsterResponseDTO;

@Service
public class DumpsterAppService {

	private final ConcurrentMap<String, DumpsterDTO> dumpsters = new ConcurrentHashMap<>();

	private final ConcurrentMap<String, ConcurrentNavigableMap<LocalDate, DumpsterReadingDTO>> readings = new ConcurrentHashMap<>();

	/**
	 * Registers a new dumpster or returns HTTP 409 if its id already exists.
	 */
	public DumpsterDTO createDumpster(CreateDumpsterDTO request) {

		DumpsterDTO dumpster = new DumpsterDTO(request.getId().trim(), request.getAddress().trim(),
				request.getPostalCode().trim(), request.getInitialCapacity());

		DumpsterDTO previous = dumpsters.putIfAbsent(dumpster.getId(), dumpster);

		if (previous != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A dumpster with id " + dumpster.getId() + " already exists");
		}

		readings.putIfAbsent(dumpster.getId(), new ConcurrentSkipListMap<>());

		return copyDumpster(dumpster);
	}

	/**
	 * Records the sensor reading for a known dumpster. A repeated update for the
	 * same date replaces that day's reading.
	 */
	public UpdateDumpsterResponseDTO updateDumpster(String id, UpdateDumpsterDTO request) {

		String normalizedId = id == null ? "" : id.trim();

		if (!dumpsters.containsKey(normalizedId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Dumpster with id " + normalizedId + " was not found");
		}

		DumpsterReadingDTO reading = new DumpsterReadingDTO(request.getDate(), request.getEstimatedContainers(),
				request.getFillLevel());

		readings.computeIfAbsent(normalizedId, key -> new ConcurrentSkipListMap<>()).put(request.getDate(), reading);

		return new UpdateDumpsterResponseDTO(normalizedId, reading.getDate(), reading.getEstimatedContainers(),
				reading.getFillLevel());
	}

	/**
	 * Read-only access for usage/status operations.
	 */
	public Optional<DumpsterDTO> findDumpsterById(String id) {

		String normalizedId = id == null ? "" : id.trim();
		DumpsterDTO dumpster = dumpsters.get(normalizedId);

		return dumpster == null ? Optional.empty() : Optional.of(copyDumpster(dumpster));
	}

	/**
	 * Returns a date-ordered snapshot of all readings for one dumpster.
	 */
	public List<DumpsterReadingDTO> findReadings(String id) {

		ConcurrentNavigableMap<LocalDate, DumpsterReadingDTO> dumpsterReadings = readings
				.get(id == null ? "" : id.trim());

		if (dumpsterReadings == null) {
			return List.of();
		}

		List<DumpsterReadingDTO> result = new ArrayList<>();

		for (DumpsterReadingDTO reading : dumpsterReadings.values()) {
			result.add(copyReading(reading));
		}

		return List.copyOf(result);
	}

	/**
	 * Returns the reading for an exact date, if one has been recorded.
	 */
	public Optional<DumpsterReadingDTO> findReading(String id, LocalDate date) {

		ConcurrentNavigableMap<LocalDate, DumpsterReadingDTO> dumpsterReadings = readings
				.get(id == null ? "" : id.trim());

		if (dumpsterReadings == null) {
			return Optional.empty();
		}

		DumpsterReadingDTO reading = dumpsterReadings.get(date);

		return reading == null ? Optional.empty() : Optional.of(copyReading(reading));
	}

	/**
	 * Returns a snapshot of all registered dumpsters.
	 */
	public List<DumpsterDTO> findAllDumpsters() {

		List<DumpsterDTO> result = new ArrayList<>();

		for (DumpsterDTO dumpster : dumpsters.values()) {
			result.add(copyDumpster(dumpster));
		}

		return List.copyOf(result);
	}

	private DumpsterDTO copyDumpster(DumpsterDTO source) {

		return new DumpsterDTO(source.getId(), source.getAddress(), source.getPostalCode(),
				source.getInitialCapacity());
	}

	private DumpsterReadingDTO copyReading(DumpsterReadingDTO source) {

		return new DumpsterReadingDTO(source.getDate(), source.getEstimatedContainers(), source.getFillLevel());
	}

	public DumpsterUsageDTO getDumpsterUsage(String id, LocalDate startDate, LocalDate endDate) {

		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
		}

		if (findDumpsterById(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dumpster not found");
		}

		List<DumpsterReadingDTO> filteredReadings = new ArrayList<>();

		for (DumpsterReadingDTO reading : findReadings(id)) {

			LocalDate date = reading.getDate();

			if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
				filteredReadings.add(reading);
			}
		}
		return new DumpsterUsageDTO(id.trim(), startDate, endDate, filteredReadings);
	}
	
	public DumpsterStatusDTO getDumpstersStatus(String postalCode, LocalDate date) {
		
		if (postalCode == null || postalCode.isBlank() || date == null) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
	    }
		 List<DumpsterStatusDTO.DumpsterInfoDTO> result = new ArrayList<>();
		 
		 for (DumpsterDTO dumpster : findAllDumpsters()) {
			 if (dumpster.getPostalCode().equals(postalCode.trim())) {
				 Optional<DumpsterReadingDTO> reading =findReading(dumpster.getId(), date);
				 
				 if (reading.isPresent()) {
		                DumpsterReadingDTO r = reading.get();
		                result.add(new DumpsterStatusDTO.DumpsterInfoDTO(
		                        dumpster.getId(),
		                        dumpster.getAddress(),
		                        r.getEstimatedContainers(),
		                        r.getFillLevel()
		                ));
				 }
			 }
		 }
		 return new DumpsterStatusDTO(postalCode.trim(), date, result);
	}
}
