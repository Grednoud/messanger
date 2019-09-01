package ru.codesteps;

import ru.codesteps.exception.AuthException;
import ru.codesteps.persistance.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.codesteps.MessagePattern.*;

public class ClientHandler implements Runnable {
    private static final Logger log = Logger.getLogger("ClientHandler");

    private Server server;
    private Socket socket;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private User user;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        new Thread(this).start();
    }

    public void run() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            authenticate();
            handleMessages();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        try {
            inputStream.close();
            outputStream.writeUTF(DISCONNECT);
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() throws IOException {
        while (true) {
            String authMessage = inputStream.readUTF();
            try {
                user = checkAuthentication(authMessage);

                if (user != null && server.getAuthService().authUser(user)) {
                    log.log(Level.INFO, "User {0} authorized successful!%n", user.getLogin());
                    server.subscribe(user.getLogin(), this);
                    outputStream.writeUTF(AUTH_SUCCESS_RESPONSE);
                    outputStream.flush();
                    break;
                } else {
                    if (user != null) {
                        log.log(Level.INFO, "Wrong authorization for user {0}", user.getLogin());
                    }
                    outputStream.writeUTF(AUTH_FAIL_RESPONSE);
                    outputStream.flush();
                }
            } catch (AuthException e) {
                outputStream.writeUTF(AUTH_FAIL_RESPONSE);
                outputStream.flush();
            }
        }
    }

    private void handleMessages() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            String text = inputStream.readUTF();
            log.log(Level.INFO, "Message from user {0}: {1}", new String[]{user.getLogin(), text});

            TextMessage msg = parseTextMessageRegx(text, user.getLogin());
            if (msg != null) {
                msg.swapUsers();
                server.broadcastMessage(msg);
            } else if (text.equals(DISCONNECT)) {
                log.log(Level.INFO, "User {0} is disconnected", user.getLogin());
                server.unsubscribe(user.getLogin());
                return;
            }
        }
    }

    private User checkAuthentication(String authMessage) throws AuthException {
        String[] authParts = authMessage.split(" ");
        if (authParts.length != 3 && authParts[0] != AUTH_TAG) {
            log.log(Level.INFO, "Incorrect authorization message {0}", authMessage);
            throw new AuthException();
        }
        return new User(-1, authParts[1], authParts[2]);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void sendConnectedMessage(String login) throws IOException {
        if (socket.isConnected()) {
            outputStream.writeUTF(String.format(CONNECTED_SEND, login));
            outputStream.flush();
        }
    }

    public void sendDisconnectedMessage(String login) throws IOException {
        if (socket.isConnected()) {
            outputStream.writeUTF(String.format(DISCONNECT_SEND, login));
            outputStream.flush();
        }
    }

    public void sendMessage(String userFrom, String text) throws IOException {
        if (socket.isConnected()) {
            outputStream.writeUTF(String.format(MESSAGE_SEND_PATTERN, userFrom, text));
        }
    }
}
