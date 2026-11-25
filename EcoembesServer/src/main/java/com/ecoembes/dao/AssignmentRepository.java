package com.ecoembes.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoembes.domain.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, String> {

}
