package com.contsocket.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlantCapacity(
		
		LocalDate date, BigDecimal availableCapacityTons
		
		) {
}