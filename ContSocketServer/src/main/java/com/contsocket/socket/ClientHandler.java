package com.contsocket.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import com.contsocket.model.AssignmentReceipt;
import com.contsocket.model.PlantCapacity;
import com.contsocket.model.SocketRequest;
import com.contsocket.model.SocketResponse;
import com.contsocket.service.ContSocketPlantService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ContSocketPlantService plantService;
    private final ObjectMapper objectMapper;

    public ClientHandler(Socket clientSocket, ContSocketPlantService plantService, ObjectMapper objectMapper) {
        this.clientSocket = clientSocket;
        this.plantService = plantService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        try (
            Socket socket = clientSocket;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                return;
            }

            SocketResponse response = processRequest(requestLine);
            out.println(objectMapper.writeValueAsString(response));

        } catch (IOException e) {
            System.err.println("[ContSocket PL-02] Error en comunicación con cliente: " + e.getMessage());
        }
    }

    private SocketResponse processRequest(String requestLine) {
        try {
            SocketRequest request = objectMapper.readValue(requestLine, SocketRequest.class);

            if (request.action() == null) {
                return SocketResponse.error("INVALID_REQUEST");
            }

            return switch (request.action().toUpperCase()) {
                case "CHECK_CAPACITY" -> {
                    PlantCapacity capacity = plantService.checkCapacity(request.date());
                    yield SocketResponse.okCapacity(capacity);
                }
                case "REGISTER_ASSIGNMENT" -> {
                    AssignmentReceipt receipt = plantService.registerAssignment(request.notice());
                    yield SocketResponse.okReceipt(receipt);
                }
                default -> SocketResponse.error("INVALID_REQUEST");
            };

        } catch (NoSuchElementException | IllegalStateException | IllegalArgumentException e) {
            return SocketResponse.error(e.getMessage());
        } catch (Exception e) {
            return SocketResponse.error("INVALID_REQUEST");
        }
    }
}