package com.plassb.facade;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AssignmentNotice(

        @NotBlank
        String assignmentId,

        @NotNull
        LocalDate date,

        @PositiveOrZero
        int totalDumpsters,

        @PositiveOrZero
        int totalPackagingContainers,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal requiredTons

) {
	
}