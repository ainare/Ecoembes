package com.ecoembes.dto;

import java.time.LocalDate;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Sensor reading request body for PUT /dumpsters/{id}. */
public class UpdateDumpsterDTO {

    @NotNull(message = "Reading date is required")
    private LocalDate date;

    @NotNull(message = "Estimated container count is required")
    @PositiveOrZero(message = "Estimated container count cannot be negative")
    private Integer estimatedContainers;

    @NotNull(message = "Fill level is required (GREEN, ORANGE or RED)")
    private FillLevel fillLevel;

    public UpdateDumpsterDTO() {
    }

    public UpdateDumpsterDTO(LocalDate date, Integer estimatedContainers,
                             FillLevel fillLevel) {
        this.date = date;
        this.estimatedContainers = estimatedContainers;
        this.fillLevel = fillLevel;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getEstimatedContainers() {
        return estimatedContainers;
    }

    public void setEstimatedContainers(Integer estimatedContainers) {
        this.estimatedContainers = estimatedContainers;
    }

    public FillLevel getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(FillLevel fillLevel) {
        this.fillLevel = fillLevel;
    }
}
