package com.ecoembes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.dao.PlantRepository;
import com.ecoembes.dto.PlantCapacityDTO;
import com.ecoembes.facade.PlantController;
import com.ecoembes.service.PlantAppService;

@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:ecoembes_plants_test;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop" })
class PlantAppServiceTest {

	@Autowired
	private PlantRepository plantRepository;

	private PlantAppService plantAppService;

	@BeforeEach
	void setUp() {
		plantRepository.deleteAll();
		plantAppService = new PlantAppService(plantRepository);
	}

	@Test
	void testGetAvailableCapacity() {
		LocalDate today = LocalDate.now();

		Double capacity = plantAppService.getCapacity("PL-01", today);

		assertEquals(25.5, capacity, 0.001);
	}

	@Test
	void testGetPlantCapacity() {
		LocalDate today = LocalDate.now();

		PlantCapacityDTO result = new PlantController(plantAppService).getCapacity("PL-01", today).getBody();

		assertEquals("PL-01", result.getPlantId());
		assertEquals(today, result.getDate());
		assertEquals(25.5, result.getAvailableCapacityTons(), 0.001);
	}

	@Test
	void testPlantNotFound() {
		LocalDate today = LocalDate.now();

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> plantAppService.getCapacity("PL-99", today));

		assertEquals(404, error.getStatusCode().value());
	}

	@Test
	void testCapacityDateNotFound() {
		LocalDate date = LocalDate.now().plusDays(11);

		ResponseStatusException error = assertThrows(ResponseStatusException.class,
				() -> plantAppService.getCapacity("PL-01", date));

		assertEquals(404, error.getStatusCode().value());
	}

	@Test
	void testSecondPlantCapacity() {
		LocalDate date = LocalDate.now().plusDays(10);

		Double capacity = plantAppService.getCapacity("PL-02", date);

		assertEquals(30.0, capacity, 0.001);
	}
}
