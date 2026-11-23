package com.ecoembes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.Assignment;
import com.ecoembes.domain.Dumpster;
import com.ecoembes.domain.DumpsterReading;
import com.ecoembes.domain.Employee;
import com.ecoembes.domain.FillLevel;
import com.ecoembes.service.AssignmentAppService;
import com.ecoembes.service.DumpsterAppService;
import com.ecoembes.service.PlantAppService;

class AssignmentAppServiceTest {

    private final Employee employee = new Employee("EMP-001", "employee@example.test", "demo");
    private DumpsterAppService dumpsterAppService;
    private PlantAppService plantAppService;
    private AssignmentAppService assignmentAppService;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        dumpsterAppService = new DumpsterAppService();
        plantAppService = new PlantAppService();
        assignmentAppService = new AssignmentAppService(dumpsterAppService, plantAppService);
        date = LocalDate.now();

        createDumpster("D-101", 75);
        createDumpster("D-102", 30);
    }

    private void createDumpster(String id, int containers) {
        dumpsterAppService.createDumpster(new Dumpster(id, "Calle Mayor 12", "48001", 100));
        dumpsterAppService.updateDumpster(id, new DumpsterReading(date, containers, FillLevel.GREEN));
    }

    @Test
    void testCreateAssignment() {
        Assignment result = assignmentAppService.assignDumpsters(
                "PL-01", date, List.of("D-101", "D-102"), employee
        );

        assertEquals("PL-01", result.plant().getId());
        assertEquals(2, result.totalDumpsters());
        assertEquals(105, result.totalPackagingContainers());
        assertEquals("EMP-001", result.employee().getEmployeeId());
    }

    @Test
    void testPlantNotFound() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters("PL-99", date, List.of("D-101"), employee)
        );
        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void testDumpsterNotFound() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters("PL-01", date, List.of("D-999"), employee)
        );
        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void testEmployeeNotAuthenticated() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters("PL-01", date, List.of("D-101"), null)
        );
        assertEquals(401, error.getStatusCode().value());
    }

    @Test
    void testRepeatedDumpster() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters(
                        "PL-01", date, List.of("D-101", " D-101 "), employee
                )
        );
        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void testEmptyDumpsterList() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters("PL-01", date, List.of(), employee)
        );
        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void testReadingNotFound() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters(
                        "PL-01", date.plusDays(1), List.of("D-101"), employee
                )
        );
        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void testAssignmentIdsAreUnique() {
        Assignment first = assignmentAppService.assignDumpsters(
                "PL-01", date, List.of("D-101"), employee
        );
        Assignment second = assignmentAppService.assignDumpsters(
                "PL-01", date, List.of("D-102"), employee
        );

        assertEquals("AS-001", first.assignmentId());
        assertEquals("AS-002", second.assignmentId());
    }

    @Test
    void testAssignmentReservesCapacity() {
        double capacityBefore = plantAppService.getCapacity("PL-01", date);

        assignmentAppService.assignDumpsters("PL-01", date, List.of("D-101"), employee);

        double capacityAfter = plantAppService.getCapacity("PL-01", date);

        // 75 envases x 0.05 kg / 1000 = 0.00375 toneladas.
        assertEquals(capacityBefore - 0.00375, capacityAfter, 0.000001);
    }

    @Test
    void testInsufficientPlantCapacity() {
        dumpsterAppService.createDumpster(new Dumpster("D-103", "Calle Mayor 12", "48001", 600000));
        dumpsterAppService.updateDumpster("D-103", new DumpsterReading(date, 510001, FillLevel.GREEN));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters("PL-01", date, List.of("D-103"), employee)
        );

        assertEquals(409, error.getStatusCode().value());
        assertEquals(25.5, plantAppService.getCapacity("PL-01", date), 0.000001);
    }

    @Test
    void testDumpsterCannotBeAssignedTwice() {
        assignmentAppService.assignDumpsters("PL-01", date, List.of("D-101"), employee);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> assignmentAppService.assignDumpsters("PL-02", date, List.of("D-101"), employee)
        );

        assertEquals(409, error.getStatusCode().value());
    }
}
