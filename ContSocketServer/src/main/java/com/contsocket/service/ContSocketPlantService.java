package com.contsocket.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.contsocket.model.AssignmentNotice;
import com.contsocket.model.AssignmentReceipt;
import com.contsocket.model.PlantCapacity;

public class ContSocketPlantService {

    private static final BigDecimal DEFAULT_DAILY_CAPACITY = new BigDecimal("30.00000");

    private final Map<LocalDate, BigDecimal> capacityByDate = new HashMap<>();
    private final Map<String, AssignmentReceipt> processedAssignments = new HashMap<>();

    public ContSocketPlantService() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 10; i++) {
            capacityByDate.put(today.plusDays(i), DEFAULT_DAILY_CAPACITY);
        }
    }

    public synchronized PlantCapacity checkCapacity(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        BigDecimal available = capacityByDate.computeIfAbsent(date, d -> DEFAULT_DAILY_CAPACITY);
        return new PlantCapacity(date, available);
    }

    public synchronized AssignmentReceipt registerAssignment(AssignmentNotice notice) {
        if (notice == null || notice.assignmentId() == null || notice.date() == null || notice.requiredTons() == null) {
            throw new IllegalArgumentException("Datos de asignación incompletos");
        }

        if (processedAssignments.containsKey(notice.assignmentId())) {
            return processedAssignments.get(notice.assignmentId());
        }

        BigDecimal available = capacityByDate.computeIfAbsent(notice.date(), d -> DEFAULT_DAILY_CAPACITY);

        if (notice.requiredTons().compareTo(BigDecimal.ZERO) <= 0 || available.compareTo(notice.requiredTons()) < 0) {
            AssignmentReceipt rejected = new AssignmentReceipt(notice.assignmentId(), false);
            processedAssignments.put(notice.assignmentId(), rejected);
            return rejected;
        }

        BigDecimal remaining = available.subtract(notice.requiredTons());
        capacityByDate.put(notice.date(), remaining);

        AssignmentReceipt accepted = new AssignmentReceipt(notice.assignmentId(), true);
        processedAssignments.put(notice.assignmentId(), accepted);
        return accepted;
    }
}