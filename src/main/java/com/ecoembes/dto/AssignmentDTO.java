package com.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

public class AssignmentDTO {

    private String assignmentId;
    private String plantId;
    private LocalDate date;
    private List<String> dumpsterIds;
    private Integer totalDumpsters;
    private Integer totalPackagingContainers;
    private String employeeId;

    public AssignmentDTO() {
    }

    public AssignmentDTO(String assignmentId, String plantId,
                         LocalDate date, List<String> dumpsterIds,
                         Integer totalDumpsters,
                         Integer totalPackagingContainers,
                         String employeeId) {

        this.assignmentId = assignmentId;
        this.plantId = plantId;
        this.date = date;
        this.dumpsterIds = dumpsterIds;
        this.totalDumpsters = totalDumpsters;
        this.totalPackagingContainers = totalPackagingContainers;
        this.employeeId = employeeId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
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

    public Integer getTotalDumpsters() {
        return totalDumpsters;
    }

    public void setTotalDumpsters(Integer totalDumpsters) {
        this.totalDumpsters = totalDumpsters;
    }

    public Integer getTotalPackagingContainers() {
        return totalPackagingContainers;
    }

    public void setTotalPackagingContainers(Integer totalPackagingContainers) {
        this.totalPackagingContainers = totalPackagingContainers;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
