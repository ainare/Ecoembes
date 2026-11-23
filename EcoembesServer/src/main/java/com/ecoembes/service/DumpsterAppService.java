package com.ecoembes.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.Dumpster;
import com.ecoembes.domain.DumpsterReading;

@Service
public class DumpsterAppService {

    private final ConcurrentMap<String, Dumpster> dumpsters = new ConcurrentHashMap<>();

    public Dumpster createDumpster(Dumpster request) {
        Dumpster dumpster = new Dumpster(
                request.id().trim(),
                request.address().trim(),
                request.postalCode().trim(),
                request.initialCapacity()
        );

        if (dumpsters.putIfAbsent(dumpster.id(), dumpster) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A dumpster with id " + dumpster.id() + " already exists"
            );
        }

        return dumpster;
    }

    public DumpsterReading updateDumpster(String id, DumpsterReading reading) {
        Dumpster dumpster = findDumpsterById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Dumpster with id " + id + " was not found"
        ));

        if (reading == null || reading.date() == null || reading.estimatedContainers() == null
                || reading.estimatedContainers() < 0 || reading.fillLevel() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading");
        }

        dumpster.saveReading(reading);
        return reading;
    }

    public Optional<Dumpster> findDumpsterById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(dumpsters.get(id.trim()));
    }

    public List<DumpsterReading> getUsage(String id, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
        }

        Dumpster dumpster = findDumpsterById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Dumpster not found"));
        return dumpster.findReadings(startDate, endDate);
    }

    public List<Dumpster> getStatus(String postalCode, LocalDate date) {
        if (postalCode == null || postalCode.isBlank() || date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        String normalizedPostalCode = postalCode.trim();
        return dumpsters.values().stream()
                .filter(dumpster -> dumpster.postalCode().equals(normalizedPostalCode))
                .filter(dumpster -> dumpster.findReading(date).isPresent())
                .sorted(Comparator.comparing(Dumpster::id))
                .toList();
    }
}
