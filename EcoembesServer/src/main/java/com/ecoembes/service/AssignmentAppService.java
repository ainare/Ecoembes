package com.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dao.AssignmentRepository;
import com.ecoembes.domain.Assignment;
import com.ecoembes.domain.AssignmentStatus;
import com.ecoembes.domain.Dumpster;
import com.ecoembes.domain.DumpsterReading;
import com.ecoembes.domain.Employee;
import com.ecoembes.domain.Plant;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.ecoembes.domain.AssignmentNotice;
import com.ecoembes.domain.AssignmentReceipt;
import com.ecoembes.external.PlantGatewayFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AssignmentAppService {

	// Peso estimado de cada envase: 50 gramos.
	private static final double KG_PER_PACKAGE = 0.05;
	private static final double KG_PER_TON = 1000.0;
	private static final BigDecimal TONS_PER_CONTAINER = new BigDecimal("0.00005");
	
	private final DumpsterAppService dumpsterAppService;
	private final PlantAppService plantAppService;
	private final AssignmentRepository assignmentRepository;
	private final PlantGatewayFactory gatewayFactory;
	private final TransactionTemplate transactionTemplate;
	

	@Autowired
	public AssignmentAppService(
	        DumpsterAppService dumpsterAppService,
	        PlantAppService plantAppService,
	        AssignmentRepository assignmentRepository,
	        PlantGatewayFactory gatewayFactory,
	        PlatformTransactionManager transactionManager) {

	    this.dumpsterAppService = dumpsterAppService;
	    this.plantAppService = plantAppService;
	    this.assignmentRepository = assignmentRepository;
	    this.gatewayFactory = gatewayFactory;
	    this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	// Constructor para mantener la compatibilidad con los tests antiguos.
	public AssignmentAppService(
	        DumpsterAppService dumpsterAppService,
	        PlantAppService plantAppService,
	        AssignmentRepository assignmentRepository) {

	    this.dumpsterAppService = dumpsterAppService;
	    this.plantAppService = plantAppService;
	    this.assignmentRepository = assignmentRepository;
	    this.gatewayFactory = null;
	    this.transactionTemplate = null;
	}

	public synchronized Assignment assignDumpsters(
	        String plantId, LocalDate date, List<String> dumpsterIds, Employee employee) {

	    // Compatibilidad con los tests antiguos que utilizan el constructor de tres parámetros.
	    if (gatewayFactory == null || transactionTemplate == null) {
	        return assignDumpstersLegacy(plantId, date, dumpsterIds, employee);
	    }

	    if (plantId == null || plantId.isBlank() || date == null
	            || dumpsterIds == null || dumpsterIds.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignment data");
	    }

	    if (employee == null || employee.getEmployeeId() == null
	            || employee.getEmployeeId().isBlank()) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee not authenticated");
	    }

	    Plant plant = plantAppService.findPlant(plantId.trim());
	    Set<String> uniqueIds = new LinkedHashSet<>();

	    for (String id : dumpsterIds) {
	        String normalizedId = id == null ? "" : id.trim();

	        if (normalizedId.isEmpty() || !uniqueIds.add(normalizedId)) {
	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST, "Invalid or repeated dumpster ID");
	        }
	    }

	    // Si ya hay una asignación pendiente idéntica, recuperamos su ID.
	    Assignment pending = findPendingAssignment(plant.getId(), date, employee.getEmployeeId(), uniqueIds);

	    if (pending != null) {
	        return sendAssignment(pending);
	    }

	    List<Dumpster> dumpsters = new ArrayList<>();
	    int totalPackagingContainers = 0;

	    for (String dumpsterId : uniqueIds) {

	        if (assignmentRepository.existsByDateAndItems_Dumpster_Id(date, dumpsterId)) {
	            throw new ResponseStatusException(
	                    HttpStatus.CONFLICT,
	                    "Dumpster already assigned for this date: " + dumpsterId);
	        }

	        Dumpster dumpster = dumpsterAppService.findDumpsterById(dumpsterId)
	                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dumpster " + dumpsterId + " not found"));

	        DumpsterReading reading = dumpsterAppService.getReading(dumpsterId, date);
	        int estimated = reading.estimatedContainers();

	        if (estimated < 0) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Negative packaging count");
	        }

	        try {
	            totalPackagingContainers = Math.addExact(
	                    totalPackagingContainers, estimated);
	        } catch (ArithmeticException ex) {
	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST, "Total packaging count is too large");
	        }

	        dumpsters.add(dumpster);
	    }

	    // Guardamos primero la asignación como PENDING.
	    final int packagingContainers = totalPackagingContainers;
	    Assignment newAssignment = transactionTemplate.execute(status -> {
	        String assignmentId = generateAssignmentId();
	        
	        Assignment assignment = new Assignment(assignmentId,plant,employee,date,
	                dumpsters,packagingContainers);
	        return assignmentRepository.save(assignment);
	    });

	    // La petición HTTP se realiza fuera de la transacción anterior.
	    return sendAssignment(newAssignment);
	}
	
	private Assignment findPendingAssignment(
	        String plantId, LocalDate date, String employeeId, Set<String> requestedIds) {

	    return transactionTemplate.execute(status -> {

	        for (Assignment assignment : assignmentRepository.findAll()) {

	            if (assignment.getStatus() == AssignmentStatus.PENDING
	                    && assignment.plant().getId().equals(plantId)
	                    && assignment.employee().getEmployeeId().equals(employeeId)
	                    && assignment.date().equals(date)
	                    && hasSameDumpsters(assignment, requestedIds)) {

	                // Cargar las relaciones antes de cerrar la transacción.
	                assignment.dumpsters().forEach(Dumpster::id);
	                assignment.plant().getId();
	                assignment.employee().getEmployeeId();

	                return assignment;
	            }
	        }

	        return null;
	    });
	}

	private boolean hasSameDumpsters(
	        Assignment assignment, Set<String> requestedIds) {

	    Set<String> existingIds = new LinkedHashSet<>();

	    for (Dumpster dumpster : assignment.dumpsters()) {
	    	existingIds.add(dumpster.id());
	    }

	    return existingIds.equals(requestedIds);
	}

	private Assignment sendAssignment(Assignment assignment) {
	    BigDecimal requiredTons = TONS_PER_CONTAINER.multiply(
	            BigDecimal.valueOf(assignment.totalPackagingContainers()));

	    AssignmentNotice notice = new AssignmentNotice(
	            assignment.assignmentId(),
	            assignment.date(),
	            assignment.totalDumpsters(),
	            assignment.totalPackagingContainers(),
	            requiredTons);

	    try {
	        AssignmentReceipt receipt = gatewayFactory
	                .forPlant(assignment.plant().getId())
	                .registerAssignment(notice);

	        if (receipt == null
	                || !receipt.accepted()
	                || !assignment.assignmentId().equals(receipt.assignmentId())) {

	            throw new ResponseStatusException(
	                    HttpStatus.CONFLICT,
	                    "PL-01 has rejected the assignment");
	        }

	        // Si PL-01 acepta, confirmamos la asignación en nuestra base de datos.
	        return transactionTemplate.execute(status -> {
	            Assignment stored = assignmentRepository.findById(assignment.assignmentId())
	                    .orElseThrow(() -> new ResponseStatusException(
	                            HttpStatus.NOT_FOUND, "Assignment not found"));

	            stored.setStatus(AssignmentStatus.CONFIRMED);

	            Assignment confirmed = assignmentRepository.save(stored);

	            // Cargar los contenedores antes de cerrar la transacción.
	            confirmed.dumpsters().forEach(Dumpster::id);

	            return confirmed;
	        });
	        
	    } catch (ResponseStatusException ex) {
	        // Si es un error definitivo, liberamos los contenedores.
	        // Si es 503, conservamos el PENDING y su ID para poder reintentarlo.
	        if (ex.getStatusCode().value() != 503) {
	            transactionTemplate.executeWithoutResult(status ->
	                    assignmentRepository.deleteById(assignment.assignmentId()));
	        }
	        throw ex;
	    }
	}
	
	
	private Assignment assignDumpstersLegacy(
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
	                    HttpStatus.BAD_REQUEST, "Invalid or repeated dumpster ID");
	        }
	    }

	    List<Dumpster> dumpsters = new ArrayList<>();
	    int totalPackagingContainers = 0;

	    for (String dumpsterId : uniqueIds) {

	        if (assignmentRepository.existsByDateAndItems_Dumpster_Id(date, dumpsterId)) {
	            throw new ResponseStatusException(
	                    HttpStatus.CONFLICT, "Dumpster already assigned for this date: " + dumpsterId);
	        }

	        Dumpster dumpster = dumpsterAppService.findDumpsterById(dumpsterId)
	                .orElseThrow(() -> new ResponseStatusException(
	                        HttpStatus.NOT_FOUND, "Dumpster " + dumpsterId + " not found"));
	        
	        DumpsterReading reading = dumpsterAppService.getReading(dumpsterId, date);

	        int estimated = reading.estimatedContainers();

	        if (estimated < 0) {
	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST, "Negative packaging count");
	        }

	        try {
	            totalPackagingContainers = Math.addExact(totalPackagingContainers, estimated);
	        } catch (ArithmeticException ex) {
	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST, "Total packaging count is too large");
	        }
	        dumpsters.add(dumpster);
	    }

	    double requiredTons = totalPackagingContainers * KG_PER_PACKAGE / KG_PER_TON;
	    plantAppService.reserveCapacity(plant.getId(), date, requiredTons);
	    String assignmentId = generateAssignmentId();
	    Assignment assignment = new Assignment(assignmentId, plant, employee, date, 
	    		dumpsters, totalPackagingContainers);

	    return assignmentRepository.save(assignment);
	}

	
	@Transactional
	public Assignment confirmAssignment(String assignmentId) {

		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

		if (assignment.getStatus() == AssignmentStatus.CONFIRMED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment already confirmed");
		}

		assignment.setStatus(AssignmentStatus.CONFIRMED);
		return assignmentRepository.save(assignment);
	}

	private String generateAssignmentId() {
		int nextId = 1;
		while (assignmentRepository.existsById(String.format("AS-%03d", nextId))) {
			nextId++;
		}
		return String.format("AS-%03d", nextId);
	}
}
