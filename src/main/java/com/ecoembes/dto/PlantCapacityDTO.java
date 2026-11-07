package com.ecoembes.dto;

import java.time.LocalDate;

public class PlantCapacityDTO {

    private String plantId;
    private LocalDate date;
    private Double availableCapacityTons;

    public PlantCapacityDTO() {
    }

    public PlantCapacityDTO(String plantId, LocalDate date,
                            Double availableCapacityTons) {
        this.plantId = plantId;
        this.date = date;
        this.availableCapacityTons = availableCapacityTons;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAvailableCapacityTons() {
        return availableCapacityTons;
    }

    public void setAvailableCapacityTons(Double availableCapacityTons) {
        this.availableCapacityTons = availableCapacityTons;
    }
}
