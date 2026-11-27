package com.contsocket.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.contsocket.model.AssignmentNotice;
import com.contsocket.model.AssignmentReceipt;
import com.contsocket.model.PlantCapacity;

public class ContSocketPlantService {

    private static final BigDecimal DEFAULT_DAILY_CAPACITY = new BigDecimal("30.00000");

    private final Map<LocalDate, BigDecimal> capacityByDate = new HashMap<>();
    private final Map<String, AssignmentNotice> receivedNotices = new HashMap<>();
    private final Map<String, AssignmentReceipt> processedAssignments = new HashMap<>();

    public ContSocketPlantService() {
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i <= 10; i++) {
            capacityByDate.put(today.plusDays(i), DEFAULT_DAILY_CAPACITY);
        }
    }

    public synchronized PlantCapacity checkCapacity(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("INVALID_REQUEST: La fecha es obligatoria");
        }
        BigDecimal available = capacityByDate.get(date);
        if (available == null) {
            throw new NoSuchElementException("DATE_NOT_FOUND");
        }
        return new PlantCapacity(date, available);
    }

    public synchronized AssignmentReceipt registerAssignment(AssignmentNotice notice) {
        if (notice == null || notice.assignmentId() == null || notice.assignmentId().isBlank()
                || notice.date() == null || notice.requiredTons() == null
                || notice.totalDumpsters() < 0 || notice.totalPackagingContainers() < 0
                || notice.requiredTons().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("INVALID_REQUEST");
        }

      
        if (receivedNotices.containsKey(notice.assignmentId())) {
            AssignmentNotice previous = receivedNotices.get(notice.assignmentId());
            boolean sameData = previous.date().equals(notice.date())
                    && previous.totalDumpsters() == notice.totalDumpsters()
                    && previous.totalPackagingContainers() == notice.totalPackagingContainers()
                    && previous.requiredTons().compareTo(notice.requiredTons()) == 0;

            if (!sameData) {
                throw new IllegalStateException("ASSIGNMENT_ID_CONFLICT");
            }
            return processedAssignments.get(notice.assignmentId());
        }

        
        BigDecimal available = capacityByDate.get(notice.date());
        if (available == null) {
            throw new NoSuchElementException("DATE_NOT_FOUND");
        }


        if (available.compareTo(notice.requiredTons()) < 0) {
            throw new IllegalStateException("INSUFFICIENT_CAPACITY");
        }


        BigDecimal remaining = available.subtract(notice.requiredTons());
        capacityByDate.put(notice.date(), remaining);

        AssignmentReceipt accepted = new AssignmentReceipt(notice.assignmentId(), true);
        receivedNotices.put(notice.assignmentId(), notice);
        processedAssignments.put(notice.assignmentId(), accepted);
        return accepted;
    }
}