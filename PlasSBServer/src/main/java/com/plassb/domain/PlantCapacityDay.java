package com.plassb.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "plant_capacity_days")
public class PlantCapacityDay {

    @Id
    @Column(name = "capacity_date")
    private LocalDate date;

    @Column(nullable = false, precision = 15, scale = 5)
    private BigDecimal availableTons;

    public PlantCapacityDay() {
    }

    public PlantCapacityDay(LocalDate date, BigDecimal availableTons) {
        this.date = date;
        this.availableTons = availableTons;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAvailableTons() {
        return availableTons;
    }

    public void setAvailableTons(BigDecimal availableTons) {
        this.availableTons = availableTons;
    }
}
