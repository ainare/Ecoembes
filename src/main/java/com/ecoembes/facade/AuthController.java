package com.ecoembes.facade;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecoembes.dto.LoginRequest;
import com.ecoembes.dto.LoginResponse;
import com.ecoembes.service.AuthService;

import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    // POST /logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            ) String authorizationHeader) {

        String token = extractToken(authorizationHeader);

        authService.logout(token);

        return ResponseEntity.noContent().build();
    }

    // Extract the token from the Authorization header
    private String extractToken(String authorizationHeader) {

        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(
                        true, 0, "Bearer ", 0, 7)) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();

        return token.isEmpty() ? null : token;
    }
}