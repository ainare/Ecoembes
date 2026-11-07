package com.ecoembes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dto.AssignmentDTO;
import com.ecoembes.dto.CreateAssignmentDTO;
import com.ecoembes.dto.CreateDumpsterDTO;
import com.ecoembes.dto.FillLevel;
import com.ecoembes.dto.UpdateDumpsterDTO;
import com.ecoembes.service.AssignmentAppService;
import com.ecoembes.service.DumpsterAppService;
import com.ecoembes.service.PlantAppService;

class AssignmentAppServiceTest {

    private DumpsterAppService dumpsterAppService;
    private AssignmentAppService assignmentAppService;
    private LocalDate date;
    private PlantAppService plantAppService;

    @BeforeEach
    void setUp() {

        dumpsterAppService = new DumpsterAppService();

        plantAppService = new PlantAppService();

        assignmentAppService = new AssignmentAppService(
                dumpsterAppService,
                plantAppService
        );

        date = LocalDate.now();

        createDumpster("D-101", 75);
        createDumpster("D-102", 30);
    }

    private void createDumpster(String id, int containers) {

        dumpsterAppService.createDumpster(
                new CreateDumpsterDTO(
                        id,
                        "Calle Mayor 12",
                        "48001",
                        100
                )
        );

        dumpsterAppService.updateDumpster(
                id,
                new UpdateDumpsterDTO(
                        date,
                        containers,
                        FillLevel.GREEN
                )
        );
    }

    @Test
    void testCreateAssignment() {

        CreateAssignmentDTO request = new CreateAssignmentDTO(
                "PL-01",
                date,
                List.of("D-101", "D-102")
        );

        AssignmentDTO result =
                assignmentAppService.createAssignment(
                        request,
                        "EMP-001"
                );

        assertEquals("PL-01", result.getPlantId());
        assertEquals(2, result.getTotalDumpsters());
        assertEquals(105, result.getTotalPackagingContainers());
        assertEquals("EMP-001", result.getEmployeeId());
    }

    @Test
    void testPlantNotFound() {

        CreateAssignmentDTO request = new CreateAssignmentDTO(
                "PL-99",
                date,
                List.of("D-101")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.createAssignment(
                        request, "EMP-001"
                )
        );

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void testDumpsterNotFound() {

        CreateAssignmentDTO request = new CreateAssignmentDTO(
                "PL-01",
                date,
                List.of("D-999")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.createAssignment(
                        request, "EMP-001"
                )
        );

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void testEmployeeNotAuthenticated() {

        CreateAssignmentDTO request = new CreateAssignmentDTO(
                "PL-01",
                date,
                List.of("D-101")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.createAssignment(
                        request, null
                )
        );

        assertEquals(401, error.getStatusCode().value());
    }

    @Test
    void testRepeatedDumpster() {

        CreateAssignmentDTO request = new CreateAssignmentDTO(
                "PL-01",
                date,
                List.of("D-101", "D-101")
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.createAssignment(
                        request, "EMP-001"
                )
        );

        assertEquals(400, error.getStatusCode().value());
    }
    

    @Test
    void testEmptyDumpsterList() {

    CreateAssignmentDTO request = new CreateAssignmentDTO(
            "PL-01",
            date,
            List.of()
    );

    ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> assignmentAppService.createAssignment(
                    request, "EMP-001"
            )
    );

    assertEquals(400, error.getStatusCode().value());
}

@Test
	void testReadingNotFound() {

    CreateAssignmentDTO request = new CreateAssignmentDTO(
            "PL-01",
            date.plusDays(1),
            List.of("D-101")
    );

    ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> assignmentAppService.createAssignment(
                    request, "EMP-001"
            )
    );

    assertEquals(404, error.getStatusCode().value());
}


	
	@Test
	void testAssignmentIdsAreUnique() {

    CreateAssignmentDTO request1 = new CreateAssignmentDTO(
            "PL-01",
            date,
            List.of("D-101")
    );

    CreateAssignmentDTO request2 = new CreateAssignmentDTO(
            "PL-01",
            date,
            List.of("D-102")
    );

    AssignmentDTO first =
            assignmentAppService.createAssignment(
                    request1, "EMP-001"
            );

    AssignmentDTO second =
            assignmentAppService.createAssignment(
                    request2, "EMP-001"
            );

    assertEquals("AS-001", first.getAssignmentId());
    assertEquals("AS-002", second.getAssignmentId());
	}
	

	@Test
	void testAssignmentReservesCapacity() {

    double capacityBefore =
            plantAppService.getAvailableCapacity("PL-01", date);

    CreateAssignmentDTO request = new CreateAssignmentDTO(
            "PL-01",
            date,
            List.of("D-101")
    );

    assignmentAppService.createAssignment(request, "EMP-001");

    double capacityAfter =
            plantAppService.getAvailableCapacity("PL-01", date);

    // 75 envases x 0.05 kg / 1000 = 0.00375 toneladas.
    double expectedCapacity = capacityBefore - 0.00375;

    assertEquals(expectedCapacity, capacityAfter, 0.000001);
}

@Test
void testInsufficientPlantCapacity() {

    // Crear un contenedor con capacidad suficiente.
    dumpsterAppService.createDumpster(
            new CreateDumpsterDTO(
                    "D-103",
                    "Calle Mayor 12",
                    "48001",
                    600000
            )
    );

    // Registrar una lectura con 510001 envases.
    dumpsterAppService.updateDumpster(
            "D-103",
            new UpdateDumpsterDTO(
                    date,
                    510001,
                    FillLevel.GREEN
            )
    );

    CreateAssignmentDTO request = new CreateAssignmentDTO(
            "PL-01",
            date,
            List.of("D-103")
    );

    ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> assignmentAppService.createAssignment(
                    request, "EMP-001"
            )
    );

    assertEquals(409, error.getStatusCode().value());

    // Comprobar que no se ha descontado capacidad.
    assertEquals(
            25.5,
            plantAppService.getAvailableCapacity("PL-01", date),
            0.000001
    );
}


	@Test
	void testDumpsterCannotBeAssignedTwice() {

    // Primera asignación del contenedor D-101 a PL-01.
    CreateAssignmentDTO firstRequest = new CreateAssignmentDTO(
            "PL-01",
            date,
            List.of("D-101")
    );

    assignmentAppService.createAssignment(
            firstRequest, "EMP-001"
    );

    // Intentamos asignar el mismo contenedor a PL-02.
    CreateAssignmentDTO secondRequest = new CreateAssignmentDTO(
            "PL-02",
            date,
            List.of("D-101")
    );

    ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> assignmentAppService.createAssignment(
                    secondRequest, "EMP-001"
            )
    );

    assertEquals(409, error.getStatusCode().value());
}



}
