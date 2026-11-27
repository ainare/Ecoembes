package com.contsocket.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssignmentNotice(
        String assignmentId,
        LocalDate date,
        int totalDumpsters,
        int totalPackagingContainers,
        BigDecimal requiredTons
) {
}