package com.plassb.facade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlantCapacityResponse(
        LocalDate date,
        BigDecimal availableCapacityTons
) {
	
}