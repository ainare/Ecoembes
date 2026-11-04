package com.ecoembes.dto;

import java.time.LocalDate;

/** A single historical sensor reading, as used in usage-query responses. */
public class DumpsterReadingDTO {

    private LocalDate date;
    private Integer estimatedContainers;
    private FillLevel fillLevel;

    public DumpsterReadingDTO() {
    }

    public DumpsterReadingDTO(LocalDate date, Integer estimatedContainers,
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
