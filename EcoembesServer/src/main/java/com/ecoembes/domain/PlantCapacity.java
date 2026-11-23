package com.ecoembes.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlantCapacity(LocalDate date, BigDecimal availableCapacityTons) {
}