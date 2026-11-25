package com.ecoembes.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecoembes.dao.EmployeeRepository;
import com.ecoembes.domain.Employee;

@Service
public class EmployeeService {

	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;

		if (employeeRepository.findByEmail("empleado@ecoembes.com").isEmpty()) {
			employeeRepository.save(new Employee("EMP-001", "empleado@ecoembes.com", "123456"));
		}

		if (employeeRepository.findByEmail("admin@ecoembes.com").isEmpty()) {
			employeeRepository.save(new Employee("EMP-002", "admin@ecoembes.com", "admin123"));
		}
	}

	public Optional<Employee> authenticate(String email, String password) {
		if (email == null || password == null) {
			return Optional.empty();
		}

		Optional<Employee> emp = employeeRepository.findByEmail(email);

		if (emp.isPresent() && emp.get().matchesPassword(password)) {
			return emp;
		}

		return Optional.empty();
	}
}
