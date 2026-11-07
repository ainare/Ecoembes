package com.ecoembes.facade;

import java.time.LocalDate;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecoembes.dto.PlantCapacityDTO;
import com.ecoembes.service.PlantAppService;

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer"
)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Plants", description = "Operaciones relacionadas con las plantas de reciclaje")
@RestController
@RequestMapping("/plants")

public class PlantController {

    private final PlantAppService plantAppService;

    public PlantController(PlantAppService plantAppService) {
        this.plantAppService = plantAppService;
    }

@io.swagger.v3.oas.annotations.Operation(
        summary = "Consultar capacidad de una planta",
        description = "Devuelve la capacidad disponible de una planta para una fecha determinada."
)
@io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Capacidad consultada correctamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Fecha o parametros incorrectos"
        ),
        
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Empleado no autenticado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Planta o capacidad no encontrada"
        )
})


    @GetMapping("/{id}/capacity")
    public ResponseEntity<PlantCapacityDTO> getPlantCapacity(
            @PathVariable("id") String id,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        PlantCapacityDTO response =
                plantAppService.getPlantCapacity(id, date);

        return ResponseEntity.ok(response);
    }
}
