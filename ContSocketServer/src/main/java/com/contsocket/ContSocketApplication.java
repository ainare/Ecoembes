package com.contsocket;

import com.contsocket.service.ContSocketPlantService;
import com.contsocket.socket.ContSocketTcpServer;

public class ContSocketApplication {

    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido en argumentos, usando puerto por defecto " + DEFAULT_PORT);
            }
        }

        ContSocketPlantService plantService = new ContSocketPlantService();
        ContSocketTcpServer server = new ContSocketTcpServer(port, plantService);
        server.start();
    }
}