package com.ecoembes.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecoembes.domain.Employee;

@Service
public class EmployeeService {

    private final Map<String, Employee> employeesDB = new HashMap<>();

    public EmployeeService() {
       
        Employee emp1 = new Employee("EMP-001", "empleado@ecoembes.com", "123456");
        Employee emp2 = new Employee("EMP-002", "admin@ecoembes.com", "admin123");
        employeesDB.put(emp1.getEmail(), emp1);
        employeesDB.put(emp2.getEmail(), emp2);
    }

    public Optional<Employee> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        Employee emp = employeesDB.get(email);
        if (emp != null && emp.matchesPassword(password)) {
            return Optional.of(emp);
        }

        return Optional.empty();
    }
}