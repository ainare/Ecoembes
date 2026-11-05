package com.ecoembes.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dto.AssignmentDTO;
import com.ecoembes.dto.CreateAssignmentDTO;
import com.ecoembes.dto.DumpsterReadingDTO;

@Service
public class AssignmentAppService {

    private final DumpsterAppService dumpsterAppService;
    private final PlantAppService plantAppService;

    private final Map<String, AssignmentDTO> assignments =
            new ConcurrentHashMap<>();

    private final AtomicInteger nextAssignmentId =
            new AtomicInteger(1);

    public AssignmentAppService(
            DumpsterAppService dumpsterAppService,
            PlantAppService plantAppService) {

        this.dumpsterAppService = dumpsterAppService;
        this.plantAppService = plantAppService;
    }

    public AssignmentDTO createAssignment(
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

        // Check that the plant and its capacity exist for this date.
        plantAppService.getAvailableCapacity(
                plantId, request.getDate()
        );

        List<String> dumpsterIds = new ArrayList<>();
        Set<String> uniqueIds = new HashSet<>();

        int totalPackagingContainers = 0;

        for (String id : request.getDumpsterIds()) {

            String dumpsterId = id == null ? "" : id.trim();

            if (dumpsterId.isEmpty() || !uniqueIds.add(dumpsterId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or repeated dumpster ID"
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

            totalPackagingContainers += reading.getEstimatedContainers();

            dumpsterIds.add(dumpsterId);
        }

        // TODO: Check available capacity in tons once the
        // conversion from packaging containers to tons is agreed.

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
