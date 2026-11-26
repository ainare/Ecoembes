package com.ecoembes.dao;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoembes.domain.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, String> {

	boolean existsByDateAndItems_Dumpster_Id(LocalDate date, String dumpsterId);

}
