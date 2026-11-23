package com.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.Assignment;
import com.ecoembes.domain.Dumpster;
import com.ecoembes.domain.DumpsterReading;
import com.ecoembes.domain.Employee;
import com.ecoembes.domain.Plant;

@Service
public class AssignmentAppService {

    // Peso estimado de cada envase: 50 gramos.
    private static final double KG_PER_PACKAGE = 0.05;
    private static final double KG_PER_TON = 1000.0;

    private final DumpsterAppService dumpsterAppService;
    private final PlantAppService plantAppService;
    private final Map<String, Assignment> assignments = new HashMap<>();
    private final Map<LocalDate, Set<String>> assignedDumpstersByDate = new HashMap<>();
    private int nextAssignmentId = 1;

    public AssignmentAppService(
            DumpsterAppService dumpsterAppService, PlantAppService plantAppService) {
        this.dumpsterAppService = dumpsterAppService;
        this.plantAppService = plantAppService;
    }

    public synchronized Assignment assignDumpsters(
            String plantId, LocalDate date, List<String> dumpsterIds, Employee employee) {
        if (plantId == null || plantId.isBlank() || date == null
                || dumpsterIds == null || dumpsterIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment data");
        }

        if (employee == null || employee.getEmployeeId() == null
                || employee.getEmployeeId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee not authenticated");
        }

        Plant plant = plantAppService.findPlant(plantId.trim());
        plantAppService.getCapacity(plant.getId(), date);

        Set<String> uniqueIds = new LinkedHashSet<>();
        for (String id : dumpsterIds) {
            String normalizedId = id == null ? "" : id.trim();
            if (normalizedId.isEmpty() || !uniqueIds.add(normalizedId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid or repeated dumpster ID"
                );
            }
        }

        Set<String> previouslyAssigned = assignedDumpstersByDate.getOrDefault(date, Set.of());
        List<Dumpster> dumpsters = new ArrayList<>();
        int totalPackagingContainers = 0;

        for (String dumpsterId : uniqueIds) {
            if (previouslyAssigned.contains(dumpsterId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Dumpster already assigned for this date: " + dumpsterId
                );
            }

            Dumpster dumpster = dumpsterAppService.findDumpsterById(dumpsterId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Dumpster " + dumpsterId + " not found"
                    ));

            DumpsterReading reading = dumpster.findReading(date)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Reading not found for dumpster " + dumpsterId
                    ));

            int estimated = reading.estimatedContainers();
            if (estimated < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Negative packaging count");
            }

            try {
                totalPackagingContainers = Math.addExact(totalPackagingContainers, estimated);
            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Total packaging count is too large"
                );
            }

            dumpsters.add(dumpster);
        }

        double requiredTons = totalPackagingContainers * KG_PER_PACKAGE / KG_PER_TON;
        plantAppService.reserveCapacity(plant.getId(), date, requiredTons);

        String assignmentId = String.format("AS-%03d", nextAssignmentId++);
        Assignment assignment = new Assignment(
                assignmentId, plant, employee, date, dumpsters, totalPackagingContainers
        );

        assignments.put(assignmentId, assignment);
        assignedDumpstersByDate.computeIfAbsent(date, key -> new HashSet<>()).addAll(uniqueIds);

        System.out.println(
                "Notification to plant " + plant.getId()
                + ": " + assignment.totalDumpsters()
                + " dumpsters and " + assignment.totalPackagingContainers()
                + " packaging containers."
        );

        return assignment;
    }
}
