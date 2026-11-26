package com.ecoembes.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoembes.domain.DumpsterReading;

public interface DumpsterReadingRepository extends JpaRepository<DumpsterReading, Long> {

	Optional<DumpsterReading> findByDumpsterIdAndDate(String dumpsterId, LocalDate date);

	List<DumpsterReading> findByDumpsterIdAndDateBetweenOrderByDateAsc(String dumpsterId, LocalDate startDate,
			LocalDate endDate);
}
