package com.ecoembes.config;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecoembes.domain.Session;
import com.ecoembes.service.SessionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/login";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public static final String AUTHENTICATED_EMPLOYEE_ATTRIBUTE =
            "authenticatedEmployee";

    private final SessionService sessionService;

    public TokenAuthenticationFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // No comprobar el token en el endpoint de login
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return LOGIN_PATH.equals(request.getServletPath());
    }

    // Comprobar la autenticación del resto de peticiones
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        // Comprobar que existe la cabecera Bearer
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(
                        true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {

            sendUnauthorizedResponse(response);
            return;
        }

        // Extraer el token
        String token = authorizationHeader
                .substring(BEARER_PREFIX.length())
                .trim();

        if (token.isEmpty()) {
            sendUnauthorizedResponse(response);
            return;
        }

        // Buscar la sesión asociada al token
        Optional<Session> session =
                sessionService.findSessionByToken(token);

        // Comprobar que el token existe y sigue activo
        if (session.isEmpty()) {
            sendUnauthorizedResponse(response);
            return;
        }

        // Guardar el empleado autenticado para utilizarlo
        // posteriormente en otros controladores
        request.setAttribute(
                AUTHENTICATED_EMPLOYEE_ATTRIBUTE,
                session.get().getEmployee()
        );

        // Continuar con la petición
        filterChain.doFilter(request, response);
    }

    // Devolver error HTTP 401
    private void sendUnauthorizedResponse(
            HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().write(
                "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":401,"
                + "\"error\":\"Unauthorized\","
                + "\"code\":\"INVALID_TOKEN\","
                + "\"message\":\"Missing or invalid authentication token.\""
                + "}"
        );
    }
}
