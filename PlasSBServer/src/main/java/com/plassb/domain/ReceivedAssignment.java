package com.plassb.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "received_assignments")
public class ReceivedAssignment {

    @Id
    @Column(name = "assignment_id")
    private String assignmentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int totalDumpsters;

    @Column(nullable = false)
    private int totalPackagingContainers;

    @Column(nullable = false, precision = 15, scale = 5)
    private BigDecimal requiredTons;

    public ReceivedAssignment() {
    }

    public ReceivedAssignment(String assignmentId, LocalDate date,
            int totalDumpsters, int totalPackagingContainers,
            BigDecimal requiredTons) {
        this.assignmentId = assignmentId;
        this.date = date;
        this.totalDumpsters = totalDumpsters;
        this.totalPackagingContainers = totalPackagingContainers;
        this.requiredTons = requiredTons;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getTotalDumpsters() {
        return totalDumpsters;
    }

    public void setTotalDumpsters(int totalDumpsters) {
        this.totalDumpsters = totalDumpsters;
    }

    public int getTotalPackagingContainers() {
        return totalPackagingContainers;
    }

    public void setTotalPackagingContainers(int totalPackagingContainers) {
        this.totalPackagingContainers = totalPackagingContainers;
    }

    public BigDecimal getRequiredTons() {
        return requiredTons;
    }

    public void setRequiredTons(BigDecimal requiredTons) {
        this.requiredTons = requiredTons;
    }
}