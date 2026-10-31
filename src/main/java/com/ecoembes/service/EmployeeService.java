package com.ecoembes.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecoembes.domain.Employee;

@Service
public class EmployeeService {

    public Optional<Employee> authenticate(String email, String password) {

        // TODO: Ane implementará la comprobación
        // de las credenciales del empleado.

        return Optional.empty();
    }
}