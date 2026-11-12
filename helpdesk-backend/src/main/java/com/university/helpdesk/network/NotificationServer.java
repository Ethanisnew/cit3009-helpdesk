package com.university.helpdesk.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificationServer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServer.class);
    private static final int PORT = 9090;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final List<PrintWriter> clientWriters = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void startServer() {
        running = true;
        // Multithreading: Run server on a background thread
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                logger.info("TCP Notification Server started on port {}", PORT);
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: {}", clientSocket.getInetAddress());

                    PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
                    clientWriters.add(out);

                    // Listen for client messages on a new thread
                    new Thread(() -> {
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                            String line;
                            while ((line = in.readLine()) != null) {
                                logger.info("Received from client: {}", line);
                            }
                        } catch (IOException e) {
                            logger.warn("Client disconnected.");
                        }
                    }).start();
                }
            } catch (IOException e) {
                if (running) logger.error("Server error", e);
            }
        }).start();
    }

    public void broadcast(String type, Long ticketId, String message) {
        String payload = type + "|" + ticketId + "|" + message;
        logger.info("Broadcasting notification: {}", payload);
        // Multithreading: Broadcast on a background thread
        new Thread(() -> {
            for (PrintWriter writer : clientWriters) {
                writer.println(payload);
            }
        }).start();
    }

    @PreDestroy
    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
    }
}