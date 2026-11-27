package com.plassb.dao;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import com.plassb.domain.PlantCapacityDay;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface PlantCapacityRepository extends JpaRepository<PlantCapacityDay, LocalDate> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<PlantCapacityDay> findByDate(LocalDate date);
}