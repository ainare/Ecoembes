package com.plassb.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.plassb.dao.PlantCapacityRepository;
import com.plassb.domain.PlantCapacityDay;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.plassb.dao.ReceivedAssignmentRepository;
import com.plassb.domain.ReceivedAssignment;
import com.plassb.facade.AssignmentNotice;
import com.plassb.facade.AssignmentReceipt;
import com.plassb.facade.AssignmentResult;

@Service
public class PlasSbService {

	    private final PlantCapacityRepository plantCapacityRepository;
	    private final ReceivedAssignmentRepository receivedAssignmentRepository;
	
	    public PlasSbService(PlantCapacityRepository plantCapacityRepository,ReceivedAssignmentRepository receivedAssignmentRepository) {
	        this.plantCapacityRepository = plantCapacityRepository;
	        this.receivedAssignmentRepository = receivedAssignmentRepository;
	    }
	    
	    @Transactional(readOnly = true)
	    public PlantCapacityDay getCapacity(LocalDate date) {
	
	        return plantCapacityRepository.findById(date).orElseThrow(() -> new CapacityNotFoundException(
	        		"No existe capacidad para la fecha " + date));
	    }
    
	    @Transactional
	    public AssignmentResult registerAssignment(AssignmentNotice notice) {
	        Optional<ReceivedAssignment> existing =
	                receivedAssignmentRepository.findById(notice.assignmentId());
	
	        if (existing.isPresent()) {
	            if (isSameAssignment(existing.get(), notice)) {
	                return new AssignmentResult(
	                        new AssignmentReceipt(notice.assignmentId(), true), false);
	            }
	
	            throw new AssignmentConflictException("El identificador ya existe con otros datos");
	        }
	
	        PlantCapacityDay capacity = plantCapacityRepository.findById(notice.date())
	                .orElseThrow(() -> new CapacityNotFoundException(
	                        "No existe capacidad para la fecha " + notice.date()));
	
	        // Volvemos a comprobar el ID después de obtener el bloqueo de capacidad.
	        existing = receivedAssignmentRepository.findById(notice.assignmentId());
	
	        if (existing.isPresent()) {
	            if (isSameAssignment(existing.get(), notice)) {
	                return new AssignmentResult(
	                        new AssignmentReceipt(notice.assignmentId(), true), false);
	            }
	
	            throw new AssignmentConflictException("El identificador ya existe con otros datos");
	        }
	
	        if (capacity.getAvailableTons().compareTo(notice.requiredTons()) < 0) {
	            throw new AssignmentConflictException("La capacidad disponible es insuficiente");
	        }
	
	        capacity.setAvailableTons(capacity.getAvailableTons().subtract(notice.requiredTons()));
	
	        plantCapacityRepository.save(capacity);
	
	        ReceivedAssignment assignment = new ReceivedAssignment(notice.assignmentId(),notice.date(),notice.totalDumpsters(),
	                notice.totalPackagingContainers(),notice.requiredTons()
	        );
	
	        receivedAssignmentRepository.save(assignment);
	
	        return new AssignmentResult(new AssignmentReceipt(notice.assignmentId(), true), true);
	    }
    
	    private boolean isSameAssignment(ReceivedAssignment existing,AssignmentNotice notice) {
	        return existing.getDate().equals(notice.date())
	                && existing.getTotalDumpsters() == notice.totalDumpsters()
	                && existing.getTotalPackagingContainers() == notice.totalPackagingContainers()
	                && existing.getRequiredTons().compareTo(notice.requiredTons()) == 0;
	    }
    
    
    
    
}