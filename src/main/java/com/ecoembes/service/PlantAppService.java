
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

    private final Map<String, Plant> plants = new ConcurrentHashMap<>();

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

    public Double getAvailableCapacity(String plantId, LocalDate date) {

        Plant plant = plants.get(plantId);

        if (plant == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Plant not found"
            );
        }

        Double capacity = plant.getAvailableCapacity(date);

        if (capacity == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Capacity information not found for this date"
            );
        }

        return capacity;
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
