package com.ecoembes.dto;

import java.time.LocalDate;

/** Response body for PUT /dumpsters/{id}, matching the agreed API example. */
public class UpdateDumpsterResponseDTO {

    private String id;
    private LocalDate date;
    private Integer estimatedContainers;
    private FillLevel fillLevel;

    public UpdateDumpsterResponseDTO() {
    }

    public UpdateDumpsterResponseDTO(String id, LocalDate date,
                                     Integer estimatedContainers, FillLevel fillLevel) {
        this.id = id;
        this.date = date;
        this.estimatedContainers = estimatedContainers;
        this.fillLevel = fillLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
