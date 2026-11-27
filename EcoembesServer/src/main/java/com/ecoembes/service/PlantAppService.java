package com.ecoembes.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.ecoembes.dao.PlantRepository;
import com.ecoembes.domain.Plant;
import org.springframework.beans.factory.annotation.Autowired;
import com.ecoembes.domain.PlantCapacity;
import com.ecoembes.external.PlantGatewayFactory;
import com.ecoembes.domain.PlantCapacity;
import com.ecoembes.external.PlantGatewayFactory;
import org.springframework.beans.factory.annotation.Autowired;
@Service
public class PlantAppService {

	private static final double EPSILON = 1e-9;

	private final PlantRepository plantRepository;
	private final PlantGatewayFactory gatewayFactory;

	
	@Autowired
	public PlantAppService(PlantRepository plantRepository,
	        PlantGatewayFactory gatewayFactory) {

	    this.plantRepository = plantRepository;
	    this.gatewayFactory = gatewayFactory;

	    if (!plantRepository.existsById("PL-01")) {
	        plantRepository.save(new Plant("PL-01", "PlasSB Ltd.", createCapacities(25.5)));
	    }

	    if (!plantRepository.existsById("PL-02")) {
	        plantRepository.save(new Plant("PL-02", "ContSocket Ltd.", createCapacities(30.0)));
	    }
	}
	
	public PlantAppService(PlantRepository plantRepository) {
	    this.plantRepository = plantRepository;
	    this.gatewayFactory = null;

	    if (!plantRepository.existsById("PL-01")) {
	        plantRepository.save(new Plant("PL-01", "PlasSB Ltd.", createCapacities(25.5)));
	    }

	    if (!plantRepository.existsById("PL-02")) {
	        plantRepository.save(new Plant("PL-02", "ContSocket Ltd.", createCapacities(30.0)));
	    }
	}
	

	private Map<LocalDate, Double> createCapacities(double capacity) {
		Map<LocalDate, Double> capacities = new HashMap<>();
		LocalDate today = LocalDate.now();

		for (int i = 0; i <= 10; i++) {
			capacities.put(today.plusDays(i), capacity);
		}

		return capacities;
	}

	public Plant findPlant(String plantId) {
		if (plantId == null || plantId.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant ID is required");
		}

		return plantRepository.findById(plantId.trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found"));
	}
	
	public PlantCapacity checkCapacity(String plantId, LocalDate date) {

	    if (plantId == null || plantId.isBlank() || date == null) {
	        throw new ResponseStatusException(
	                HttpStatus.BAD_REQUEST,
	                "Plant ID and date are required");
	    }

	    findPlant(plantId);

	    return gatewayFactory.forPlant(plantId.trim()).checkCapacity(date);
	}

	public synchronized Double getCapacity(String plantId, LocalDate date) {
		if (plantId == null || plantId.isBlank() || date == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plant ID and date are required");
		}

		Plant plant = findPlant(plantId);
		Double capacity = plant.getAvailableCapacity(date);

		if (capacity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Capacity information not found for this date");
		}

		return capacity;
	}

	public synchronized void reserveCapacity(String plantId, LocalDate date, double requiredTons) {

		if (!Double.isFinite(requiredTons) || requiredTons < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid weight in tons");
		}

		Plant plant = findPlant(plantId);
		double available = getCapacity(plantId, date);

		if (requiredTons > available + EPSILON) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient recycling plant capacity");
		}

		plant.setAvailableCapacity(date, Math.max(0.0, available - requiredTons));

		plantRepository.save(plant);
	}
}
