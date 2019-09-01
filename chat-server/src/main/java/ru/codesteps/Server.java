package ru.codesteps;

import ru.codesteps.auth.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger log = Logger.getLogger("Server");

    private int port;
    private AuthService authService;

    private Map<String, ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        clients = new TreeMap<>();
    }

    public void start() {
        try (ServerSocket socket = new ServerSocket(port)) {
            log.log(Level.INFO, "Server started! Waiting for clients...");
            while (true) {
                Socket client = socket.accept();
                log.log(Level.INFO, "Client connected!");
                ClientHandler clientHandler = new ClientHandler(this, client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String login, ClientHandler client) throws IOException {
        clients.put(login, client);
        sendUserConnectedMessage(login);
    }

    private void sendUserConnectedMessage(String login) throws IOException {
        for (ClientHandler client : clients.values()) {
            if (!login.equals(client.getUser().getLogin())) {
                log.log(Level.INFO, "Sending disconnect notification to {0} about {1}", new String[]{client.getUser().getLogin(), login});
                client.sendConnectedMessage(login);
            }
        }
    }

    public void unsubscribe(String login) throws IOException {
        clients.remove(login);
        sendUserDisconnectedMessage(login);
    }

    private void sendUserDisconnectedMessage(String login) throws IOException {
        for (ClientHandler client : clients.values()) {
            if (!login.equals(client.getUser().getLogin())) {
                log.log(Level.INFO, "Sending disconnect notification to {0} about {1}", new String[]{client.getUser().getLogin(), login});
                client.sendDisconnectedMessage(login);
            }
        }
    }

    public void unicastMessage() {
        //TODO
    }

    public void broadcastMessage(TextMessage msg) throws IOException {
        ClientHandler userTo = clients.get(msg.getUserTo());
        if (userTo != null) {
            userTo.sendMessage(msg.getUserFrom(), msg.getText());
        } else {
            log.log(Level.INFO, "User {0} not connected%n", msg.getUserTo());
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
