package com.ecoembes.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.Employee;

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
    public String login(String email, String password) {
        Employee employee = employeeService.authenticate(email, password)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"
                ));

        String token = String.valueOf(System.currentTimeMillis());
        sessionService.createSession(token, employee);
        return token;
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
