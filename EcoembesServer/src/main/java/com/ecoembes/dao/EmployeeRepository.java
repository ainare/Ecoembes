package com.ecoembes.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoembes.domain.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

	Optional<Employee> findByEmail(String email);

}
