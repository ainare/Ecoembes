package com.ecoembes.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.Employee;
import com.ecoembes.dto.LoginRequest;
import com.ecoembes.dto.LoginResponse;

@Service
public class AuthService {

    private final EmployeeService employeeService;
    private final SessionService sessionService;

    public AuthService(EmployeeService employeeService,
                       SessionService sessionService) {
        this.employeeService = employeeService;
        this.sessionService = sessionService;
    }

    // Login
    public LoginResponse login(LoginRequest request) {

        // Comprobar las credenciales del empleado
        Employee employee = employeeService.authenticate(
                request.getEmail(),
                request.getPassword()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                )
        );

        // Generar el token utilizando el timestamp actual
        String token = String.valueOf(System.currentTimeMillis());

        // Guardar la sesión del empleado
        sessionService.createSession(token, employee);

        // Devolver el token al cliente
        return new LoginResponse(token);
    }

    // Logout
    public void logout(String token) {

        // Comprobar que existe una sesión asociada al token
        if (token == null || token.isBlank()
                || sessionService.findSessionByToken(token).isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }

        // Eliminar la sesión
        sessionService.deleteSession(token);
    }
}