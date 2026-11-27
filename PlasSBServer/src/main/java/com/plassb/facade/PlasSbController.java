package com.plassb.facade;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.plassb.service.PlasSbService;

@RestController
public class PlasSbController {
	
	    private final PlasSbService plasSbService;
	
	    public PlasSbController(PlasSbService plasSbService) {
	        this.plasSbService = plasSbService;
	    }
	
	    @GetMapping("/capacity")
	    public PlantCapacityResponse getCapacity(@RequestParam("date") LocalDate date) {
	
	        var capacity = plasSbService.getCapacity(date);
	
	        return new PlantCapacityResponse(capacity.getDate(),capacity.getAvailableTons()
	        );
	    }
	
	    @PostMapping("/assignments")
	    public ResponseEntity<AssignmentReceipt> registerAssignment(
	            @Valid @RequestBody AssignmentNotice notice) {
	
	        AssignmentResult result =plasSbService.registerAssignment(notice);
	
	        HttpStatus status = result.created()
	                ? HttpStatus.CREATED
	                : HttpStatus.OK;
	
	        return ResponseEntity.status(status).body(result.receipt());
	    }
	}