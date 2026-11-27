package com.ecoembes.facade;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoembes.config.TokenAuthenticationFilter;
import com.ecoembes.domain.Assignment;
import com.ecoembes.domain.Employee;
import com.ecoembes.dto.AssignmentDTO;
import com.ecoembes.dto.CreateAssignmentDTO;
import com.ecoembes.service.AssignmentAppService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Assignments", description = "Operaciones para asignar contenedores a las plantas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/assignments")
public class AssignmentController {

	private final AssignmentAppService assignmentAppService;

	public AssignmentController(AssignmentAppService assignmentAppService) {
		this.assignmentAppService = assignmentAppService;
	}

	@Operation(summary = "Crear una asignación", description = "Asigna varios contenedores a una planta de reciclaje para una fecha determinada.")
	@ApiResponse(responseCode = "201", description = "Asignación creada correctamente")
	@ApiResponse(responseCode = "400", description = "Datos de asignación incorrectos")
	@ApiResponse(responseCode = "401", description = "Empleado no autenticado")
	@ApiResponse(responseCode = "404", description = "Planta, contenedor o lectura no encontrada")
	@ApiResponse(responseCode = "409", description = "Capacidad insuficiente o contenedor ya asignado")
	@ApiResponse(responseCode = "500", description = "Internal error during assignment")
	@PostMapping
	public ResponseEntity<AssignmentDTO> createAssignment(
			@RequestAttribute(TokenAuthenticationFilter.AUTHENTICATED_EMPLOYEE_ATTRIBUTE) Employee employee,
			@Valid @RequestBody CreateAssignmentDTO request) {
		Assignment assignment = assignmentAppService.assignDumpsters(request.getPlantId(), request.getDate(),
				request.getDumpsterIds(), employee);

		List<String> dumpsterIds = request.getDumpsterIds().stream()
		        .map(String::trim)
		        .toList();

		AssignmentDTO response = new AssignmentDTO(
		        assignment.assignmentId(),
		        request.getPlantId(),
		        assignment.date(),
		        dumpsterIds,
		        assignment.totalDumpsters(),
		        assignment.totalPackagingContainers(),
		        employee.getEmployeeId()
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Confirmar una asignación", description = "Cambia el estado de una asignación de PENDING a CONFIRMED.")
	@ApiResponse(responseCode = "204", description = "Asignación confirmada")
	@ApiResponse(responseCode = "401", description = "Empleado no autenticado")
	@ApiResponse(responseCode = "404", description = "Asignación no encontrada")
	@ApiResponse(responseCode = "409", description = "Asignación ya confirmada")
	@PostMapping("/{id}/confirm")
	public ResponseEntity<Void> confirmAssignment(@PathVariable("id") String id) {

		assignmentAppService.confirmAssignment(id);

		return ResponseEntity.noContent().build();
	}

}
