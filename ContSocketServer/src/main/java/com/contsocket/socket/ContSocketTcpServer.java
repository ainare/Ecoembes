package com.contsocket.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.contsocket.service.ContSocketPlantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ContSocketTcpServer {

    private final int port;
    private final ContSocketPlantService plantService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    public ContSocketTcpServer(int port, ContSocketPlantService plantService) {
        this.port = port;
        this.plantService = plantService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("==================================================");
            System.out.println("  Servidor TCP ContSocket (PL-02) iniciado");
            System.out.println("  Escuchando en el puerto: " + port);
            System.out.println("==================================================");

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(new ClientHandler(clientSocket, plantService, objectMapper));
            }
        } catch (IOException e) {
            System.err.println("[ContSocket PL-02] Error en el servidor TCP: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }
}