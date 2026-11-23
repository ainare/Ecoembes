package com.ecoembes.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.Plant;

@Service
public class PlantAppService {

    private static final double EPSILON = 1e-9;

    private final Map<String, Plant> plants;
    private final Map<String, Map<LocalDate, Double>> reservedTons = new HashMap<>();

    public PlantAppService() {
        plants = Map.of(
                "PL-01", new Plant("PL-01", "PlasSB Ltd.", createCapacities(25.5)),
                "PL-02", new Plant("PL-02", "ContSocket Ltd.", createCapacities(30.0))
        );
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

        Plant plant = plants.get(plantId);
        if (plant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plant not found");
        }
        return plant;
    }

    public synchronized Double getCapacity(String plantId, LocalDate date) {
        if (plantId == null || plantId.isBlank() || date == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Plant ID and date are required"
            );
        }

        Plant plant = findPlant(plantId);
        Double initialCapacity = plant.getAvailableCapacity(date);

        if (initialCapacity == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Capacity information not found for this date"
            );
        }

        double alreadyReserved = reservedTons
                .getOrDefault(plantId, Map.of())
                .getOrDefault(date, 0.0);

        return Math.max(0.0, initialCapacity - alreadyReserved);
    }

    public synchronized void reserveCapacity(String plantId, LocalDate date, double requiredTons) {
        if (!Double.isFinite(requiredTons) || requiredTons < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid weight in tons");
        }

        double available = getCapacity(plantId, date);
        if (requiredTons > available + EPSILON) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Insufficient recycling plant capacity"
            );
        }

        reservedTons
                .computeIfAbsent(plantId, key -> new HashMap<>())
                .merge(date, requiredTons, Double::sum);
    }
}
