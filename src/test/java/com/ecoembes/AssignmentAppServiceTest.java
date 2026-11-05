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

    @BeforeEach
    void setUp() {

        dumpsterAppService = new DumpsterAppService();

        PlantAppService plantAppService = new PlantAppService();

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

    CreateAssignmentDTO request = new CreateAssignmentDTO(
            "PL-01",
            date,
            List.of("D-101")
    );

    AssignmentDTO first =
            assignmentAppService.createAssignment(
                    request, "EMP-001"
            );

    AssignmentDTO second =
            assignmentAppService.createAssignment(
                    request, "EMP-001"
            );

    assertEquals("AS-001", first.getAssignmentId());
    assertEquals("AS-002", second.getAssignmentId());
}


}
