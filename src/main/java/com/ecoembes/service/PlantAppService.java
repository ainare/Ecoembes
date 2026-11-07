
package com.ecoembes.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.Plant;
import com.ecoembes.dto.PlantCapacityDTO;

@Service
public class PlantAppService {
	private static final double EPSILON = 1e-9;
    private final Map<String, Plant> plants = new ConcurrentHashMap<>();
 // Toneladas ya reservadas por planta y fecha.
    private final Map<String, Map<LocalDate, Double>> reservedTons = new HashMap<>();

    public PlantAppService() {

        plants.put("PL-01", new Plant(
                "PL-01",
                "PlasSB Ltd.",
                createCapacities(25.5)
        ));

        plants.put("PL-02", new Plant(
                "PL-02",
                "ContSocket Ltd.",
                createCapacities(30.0)
        ));
    }

    private Map<LocalDate, Double> createCapacities(double capacity) {

        Map<LocalDate, Double> capacities = new HashMap<>();

        LocalDate today = LocalDate.now();

        for (int i = 0; i <= 10; i++) {
            capacities.put(today.plusDays(i), capacity);
        }

        return capacities;
    }


    public synchronized Double getAvailableCapacity(
        String plantId, LocalDate date) {

    if (plantId == null || plantId.isBlank() || date == null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Plant ID and date are required"
        );
    }

    Plant plant = plants.get(plantId);

    if (plant == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Plant not found"
        );
    }

    Double initialCapacity = plant.getAvailableCapacity(date);

    if (initialCapacity == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Capacity information not found for this date"
        );
    }

    double alreadyReserved = reservedTons
            .getOrDefault(plantId, Map.of())
            .getOrDefault(date, 0.0);

    return Math.max(0.0, initialCapacity - alreadyReserved);
}

	public synchronized void reserveCapacity(
        String plantId, LocalDate date, double requiredTons) {

    if (!Double.isFinite(requiredTons) || requiredTons < 0) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid weight in tons"
        );
    }

    double available = getAvailableCapacity(plantId, date);

    if (requiredTons > available + EPSILON) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Insufficient recycling plant capacity"
        );
    }

    reservedTons
            .computeIfAbsent(plantId, key -> new HashMap<>())
            .merge(date, requiredTons, Double::sum);
}

    
    public PlantCapacityDTO getPlantCapacity(String plantId, LocalDate date) {

        Double capacity = getAvailableCapacity(plantId, date);

        return new PlantCapacityDTO(
                plantId,
                date,
                capacity
        );
    }
    
    
    
    
}
