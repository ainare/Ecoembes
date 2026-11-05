package com.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateAssignmentDTO {

    @NotBlank
    private String plantId;

    @NotNull
    private LocalDate date;

    @NotEmpty
    private List<String> dumpsterIds;

    public CreateAssignmentDTO() {
    }

    public CreateAssignmentDTO(String plantId, LocalDate date,
                               List<String> dumpsterIds) {
        this.plantId = plantId;
        this.date = date;
        this.dumpsterIds = dumpsterIds;
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

    public List<String> getDumpsterIds() {
        return dumpsterIds;
    }

    public void setDumpsterIds(List<String> dumpsterIds) {
        this.dumpsterIds = dumpsterIds;
    }
}
