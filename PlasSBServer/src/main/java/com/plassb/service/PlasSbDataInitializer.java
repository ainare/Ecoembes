package com.plassb.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.plassb.dao.PlantCapacityRepository;
import com.plassb.domain.PlantCapacityDay;

@Component
public class PlasSbDataInitializer implements CommandLineRunner {

	    private final PlantCapacityRepository plantCapacityRepository;
	
	    public PlasSbDataInitializer(PlantCapacityRepository plantCapacityRepository) {
	        this.plantCapacityRepository = plantCapacityRepository;
	    }
	
	    @Override
	    public void run(String... args) {
	        LocalDate today = LocalDate.now();
	
	        for (int i = 0; i <= 10; i++) {
	            LocalDate date = today.plusDays(i);
	            if (!plantCapacityRepository.existsById(date)) {
	                PlantCapacityDay capacity = new PlantCapacityDay(date,new BigDecimal("25.50000"));
	                plantCapacityRepository.save(capacity);
	            }
	        }
	    }
	}