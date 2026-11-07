package com.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dto.AssignmentDTO;
import com.ecoembes.dto.CreateAssignmentDTO;
import com.ecoembes.dto.DumpsterReadingDTO;

@Service
public class AssignmentAppService {
	// Peso estimado de cada envase: 50 gramos.
	private static final double KG_PER_PACKAGE = 0.05;

	// Una tonelada equivale a 1000 kg.
	private static final double KG_PER_TON = 1000.0;
    private final DumpsterAppService dumpsterAppService;
    private final PlantAppService plantAppService;

    private final Map<String, AssignmentDTO> assignments =
            new ConcurrentHashMap<>();
    
    private final Map<LocalDate, Set<String>> assignedDumpstersByDate =
            new HashMap<>();

    private final AtomicInteger nextAssignmentId =
            new AtomicInteger(1);

    public AssignmentAppService(
            DumpsterAppService dumpsterAppService,
            PlantAppService plantAppService) {

        this.dumpsterAppService = dumpsterAppService;
        this.plantAppService = plantAppService;
    }

    public synchronized AssignmentDTO createAssignment(
            CreateAssignmentDTO request, String employeeId) {

        if (request == null || request.getPlantId() == null
                || request.getPlantId().isBlank()
                || request.getDate() == null
                || request.getDumpsterIds() == null
                || request.getDumpsterIds().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid assignment data"
            );
        }

        if (employeeId == null || employeeId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Employee not authenticated"
            );
        }

        String plantId = request.getPlantId().trim();
        LocalDate date = request.getDate();

        // Check that the plant and its capacity exist for this date.
        plantAppService.getAvailableCapacity(plantId, date);

        List<String> dumpsterIds = new ArrayList<>();
        Set<String> uniqueIds = new HashSet<>();
        Set<String> previouslyAssigned = assignedDumpstersByDate.getOrDefault(date, Collections.emptySet());

        int totalPackagingContainers = 0;

        for (String id : request.getDumpsterIds()) {

            String dumpsterId = id == null ? "" : id.trim();

            if (dumpsterId.isEmpty() || !uniqueIds.add(dumpsterId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or repeated dumpster ID"
                );
            }
            
            if (previouslyAssigned.contains(dumpsterId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Dumpster already assigned for this date: " + dumpsterId
                );
            }

            // Check that the dumpster exists.
            if (dumpsterAppService.findDumpsterById(dumpsterId).isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Dumpster " + dumpsterId + " not found"
                );
            }

            // Get the sensor reading for the selected date.
            DumpsterReadingDTO reading =
                    dumpsterAppService.findReading(
                            dumpsterId, request.getDate()
                    ).orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Reading not found for dumpster "
                                            + dumpsterId
                            )
                    );

            int estimated = reading.getEstimatedContainers();

            if (estimated < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Negative packaging count"
                );
            }

            try {
                totalPackagingContainers = Math.addExact(
                        totalPackagingContainers, estimated
                );
            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Total packaging count is too large"
                );
            }

            dumpsterIds.add(dumpsterId);
        }

        // TODO: Check available capacity in tons once the
        // conversion from packaging containers to tons is agreed.
     // Convertir el total de envases a toneladas.
        double requiredTons = totalPackagingContainers
                * KG_PER_PACKAGE / KG_PER_TON;

        // Comprobar y reservar la capacidad de la planta.
        plantAppService.reserveCapacity(plantId, date, requiredTons);
        
        String assignmentId = String.format(
                "AS-%03d", nextAssignmentId.getAndIncrement()
        );

        AssignmentDTO assignment = new AssignmentDTO(
                assignmentId,
                plantId,
                request.getDate(),
                List.copyOf(dumpsterIds),
                dumpsterIds.size(),
                totalPackagingContainers,
                employeeId
        );

        assignments.put(assignmentId, assignment);
        assignedDumpstersByDate.computeIfAbsent(date, key -> new HashSet<>()).addAll(dumpsterIds);

        // Simulate notification to the recycling plant.
        System.out.println(
                "Notification to plant " + plantId
                + ": " + assignment.getTotalDumpsters()
                + " dumpsters and "
                + assignment.getTotalPackagingContainers()
                + " packaging containers."
        );

        return assignment;
    }
}
