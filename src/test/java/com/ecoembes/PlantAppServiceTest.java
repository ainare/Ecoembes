package com.ecoembes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dto.PlantCapacityDTO;
import com.ecoembes.service.PlantAppService;

class PlantAppServiceTest {

    private final PlantAppService plantAppService =
            new PlantAppService();

    @Test
    void testGetAvailableCapacity() {

        LocalDate today = LocalDate.now();

        Double capacity =
                plantAppService.getAvailableCapacity("PL-01", today);

        assertEquals(25.5, capacity, 0.001);
    }

    @Test
    void testGetPlantCapacity() {

        LocalDate today = LocalDate.now();

        PlantCapacityDTO result =
                plantAppService.getPlantCapacity("PL-01", today);

        assertEquals("PL-01", result.getPlantId());
        assertEquals(today, result.getDate());
        assertEquals(25.5, result.getAvailableCapacityTons(), 0.001);
    }

    @Test
    void testPlantNotFound() {

        LocalDate today = LocalDate.now();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> plantAppService.getAvailableCapacity("PL-99", today)
        );

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void testCapacityDateNotFound() {

        LocalDate date = LocalDate.now().plusDays(11);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> plantAppService.getAvailableCapacity("PL-01", date)
        );

        assertEquals(404, error.getStatusCode().value());
    }
    @Test
    void testSecondPlantCapacity() {

        LocalDate date = LocalDate.now().plusDays(10);

        Double capacity =
                plantAppService.getAvailableCapacity("PL-02", date);

        assertEquals(30.0, capacity, 0.001);
    }
    
}
