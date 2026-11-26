package com.ecoembes.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dao.DumpsterRepository;
import com.ecoembes.dao.DumpsterReadingRepository;
import com.ecoembes.domain.Dumpster;
import com.ecoembes.domain.DumpsterReading;

@Service
public class DumpsterAppService {

	private final DumpsterRepository dumpsterRepository;
	private final DumpsterReadingRepository readingRepository;

	public DumpsterAppService(DumpsterRepository dumpsterRepository, DumpsterReadingRepository readingRepository) {
		this.dumpsterRepository = dumpsterRepository;
		this.readingRepository = readingRepository;
	}

	public Dumpster createDumpster(Dumpster request) {

		Dumpster dumpster = new Dumpster(request.id().trim(), request.address().trim(), request.postalCode().trim(),
				request.initialCapacity());

		if (dumpsterRepository.existsById(dumpster.id())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A dumpster with id " + dumpster.id() + " already exists");
		}

		return dumpsterRepository.save(dumpster);
	}

	public DumpsterReading updateDumpster(String id, DumpsterReading reading) {

		Dumpster dumpster = findDumpsterById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dumpster with id " + id + " was not found"));

		if (reading == null || reading.date() == null || reading.estimatedContainers() == null
				|| reading.estimatedContainers() < 0 || reading.fillLevel() == null) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading");
		}

		reading.setDumpster(dumpster);

		Optional<DumpsterReading> existing = readingRepository.findByDumpsterIdAndDate(id, reading.date());

		if (existing.isPresent()) {
			DumpsterReading previous = existing.get();

			readingRepository.delete(previous);
			readingRepository.flush();
		}

		return readingRepository.save(reading);
	}

	public Optional<Dumpster> findDumpsterById(String id) {
		return id == null ? Optional.empty() : dumpsterRepository.findById(id.trim());
	}

	public List<DumpsterReading> getUsage(String id, LocalDate startDate, LocalDate endDate) {

		if (startDate == null || endDate == null || startDate.isAfter(endDate)) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
		}

		findDumpsterById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dumpster not found"));

		return readingRepository.findByDumpsterIdAndDateBetweenOrderByDateAsc(id.trim(), startDate, endDate);
	}

	public List<Dumpster> getStatus(String postalCode, LocalDate date) {

		if (postalCode == null || postalCode.isBlank() || date == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
		}

		return dumpsterRepository.findByPostalCode(postalCode.trim()).stream()
				.filter(dumpster -> readingRepository.findByDumpsterIdAndDate(dumpster.id(), date).isPresent())
				.sorted(Comparator.comparing(Dumpster::id)).toList();
	}

	public DumpsterReading getReading(String dumpsterId, LocalDate date) {
		return readingRepository.findByDumpsterIdAndDate(dumpsterId, date)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading not found"));
	}

}
