package com.ecoembes.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoembes.config.TokenAuthenticationFilter;
import com.ecoembes.domain.Employee;
import com.ecoembes.dto.AssignmentDTO;
import com.ecoembes.dto.CreateAssignmentDTO;
import com.ecoembes.service.AssignmentAppService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;



@io.swagger.v3.oas.annotations.tags.Tag(
        name = "Assignments",
        description = "Operaciones para asignar contenedores a las plantas"
)

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/assignments")

public class AssignmentController {

    private final AssignmentAppService assignmentAppService;

    public AssignmentController(
            AssignmentAppService assignmentAppService) {
        this.assignmentAppService = assignmentAppService;
    }


@io.swagger.v3.oas.annotations.Operation(
        summary = "Crear una asignación",
        description = "Asigna varios contenedores a una planta de reciclaje para una fecha determinada."
)
@io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Asignación creada correctamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Datos de asignación incorrectos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Empleado no autenticado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Planta, contenedor o lectura no encontrada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Insufficient recycling plant capacity"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Internal error during assignment"
        )
})


    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(
            @RequestAttribute(
                TokenAuthenticationFilter.AUTHENTICATED_EMPLOYEE_ATTRIBUTE
            ) Employee employee,
            @Valid @RequestBody CreateAssignmentDTO request) {

        AssignmentDTO response =
                assignmentAppService.createAssignment(
                        request,
                        employee.getEmployeeId()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
