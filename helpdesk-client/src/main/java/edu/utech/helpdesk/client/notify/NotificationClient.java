package edu.utech.helpdesk.client.notify;

import edu.utech.helpdesk.client.model.NotificationEvent;
import edu.utech.helpdesk.client.model.NotificationType;
import edu.utech.helpdesk.client.model.UserDto;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Client side of the TCP Notification Server.
 *
 * Wire protocol (one UTF-8 line per message, agree any change with the Systems developer):
 *   client -> server on connect :  HELLO|userId|ROLE
 *   server -> client            :  TYPE|ticketId|human readable message
 *   TYPE = TICKET_ASSIGNED | TICKET_RESOLVED | COMMENT_ADDED | URGENT_TICKET_CREATED
 *
 * Runs on its own daemon thread and reconnects every 5 seconds, so the GUI never waits on the network.
 * Host/port: -Dhelpdesk.notify.host=localhost  -Dhelpdesk.notify.port=9090
 */
public final class NotificationClient {

    private static final System.Logger LOG = System.getLogger(NotificationClient.class.getName());

    private final String host = System.getProperty("helpdesk.notify.host", "localhost");
    private final int port = Integer.getInteger("helpdesk.notify.port", 9090);
    private final NotificationHub hub = NotificationHub.get();

    private volatile boolean running;
    private volatile Socket socket;
    private Thread worker;

    public void start(UserDto user) {
        running = true;
        worker = new Thread(() -> loop(user), "notification-client");
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        running = false;
        Socket s = socket;
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignored) {
                // closing anyway
            }
        }
        if (worker != null) {
            worker.interrupt();
        }
        setConnected(false);
    }

    private void loop(UserDto user) {
        while (running) {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(host, port), 3000);
                socket = s;
                setConnected(true);
                LOG.log(System.Logger.Level.INFO, "Connected to notification server {0}:{1}", host, port);

                PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                out.println("HELLO|" + user.id() + "|" + user.role());

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while (running && (line = in.readLine()) != null) {
                    handle(line);
                }
            } catch (IOException e) {
                if (running) {
                    LOG.log(System.Logger.Level.DEBUG, "Notification server unavailable: {0}", e.getMessage());
                }
            } finally {
                socket = null;
                setConnected(false);
            }
            if (!running) {
                break;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void handle(String line) {
        String[] parts = line.split("\\|", 3);
        if (parts.length < 3) {
            LOG.log(System.Logger.Level.WARNING, "Ignoring malformed notification: {0}", line);
            return;
        }
        NotificationType type = NotificationType.parse(parts[0]);
        Long ticketId = null;
        try {
            ticketId = Long.parseLong(parts[1].trim());
        } catch (NumberFormatException ignored) {
            // ticket id is optional
        }
        NotificationEvent event = new NotificationEvent(type, ticketId, parts[2], LocalDateTime.now());
        Platform.runLater(() -> hub.post(event));
    }

    private void setConnected(boolean value) {
        Platform.runLater(() -> hub.connectedProperty().set(value));
    }
}
