package com.ecoembes.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ecoembes.domain.AssignmentNotice;
import com.ecoembes.domain.AssignmentReceipt;
import com.ecoembes.domain.PlantCapacity;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class ContSocketGateway implements PlantGateway {

    private final String host;
    private final int port;
    private final Duration timeout;
    private final JsonMapper jsonMapper;

    public ContSocketGateway(String host, int port, Duration timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Override
    public PlantCapacity checkCapacity(LocalDate date) {
        SocketRequest request = new SocketRequest("CHECK_CAPACITY", date, null);
        SocketResponse response = send(request);

        if (!response.success() || response.capacity() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    response.errorMessage() != null ? response.errorMessage() : "Error al consultar capacidad en PL-02");
        }
        return response.capacity();
    }

    @Override
    public AssignmentReceipt registerAssignment(AssignmentNotice notice) {
        SocketRequest request = new SocketRequest("REGISTER_ASSIGNMENT", null, notice);
        SocketResponse response = send(request);

        if (!response.success() || response.receipt() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    response.errorMessage() != null ? response.errorMessage() : "PL-02 ha rechazado la asignación");
        }
        return response.receipt();
    }

    private SocketResponse send(SocketRequest request) {
        String jsonRequest;
        try {
            jsonRequest = jsonMapper.writeValueAsString(request);
        } catch (JacksonException e) {
            throw new IllegalStateException("No se pudo serializar la petición para PL-02", e);
        }

        int timeoutMs = (int) timeout.toMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);

            try (
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                out.println(jsonRequest);
                String jsonResponse = in.readLine();

                if (jsonResponse == null || jsonResponse.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Respuesta vacía de PL-02");
                }

                return jsonMapper.readValue(jsonResponse, SocketResponse.class);
            }
        } catch (SocketTimeoutException e) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Tiempo de espera agotado contactando con PL-02", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se puede contactar con PL-02 en " + host + ":" + port, e);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Respuesta JSON inválida de PL-02", e);
        }
    }

    private record SocketRequest(String action, LocalDate date, AssignmentNotice notice) {
    }

    private record SocketResponse(boolean success, String errorMessage, PlantCapacity capacity, AssignmentReceipt receipt) {
    }
}