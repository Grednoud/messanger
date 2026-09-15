package ru.codesteps.server;

import ru.codesteps.protocol.Message;
import ru.codesteps.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientHandler обрабатывает соединение с одним клиентом.
 */
final class ClientHandler implements Runnable {

    private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());

    private final ChatServer server;
    private final Socket socket;
    private final AuthService authService;

    private PrintWriter out;
    private BufferedReader in;
    private String login;

    ClientHandler(ChatServer server, Socket socket, AuthService authService) {
        this.server = server;
        this.socket = socket;
        this.authService = authService;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            if (!authenticate()) {
                return;
            }

            handleMessages();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Ошибка соединения: {0}", e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Выполняет аутентификацию клиента.
     */
    private boolean authenticate() throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            Message message = Protocol.deserialize(line);

            if (message instanceof Message.Auth auth) {
                if (authService.authenticate(auth.login(), auth.password())) {
                    if (server.isUserOnline(auth.login())) {
                        send(Protocol.serialize(Message.AuthResult.fail("Пользователь уже в сети")));
                        continue;
                    }

                    this.login = auth.login();
                    server.registerClient(login, this);
                    send(Protocol.serialize(Message.AuthResult.ok()));
                    return true;
                } else {
                    send(Protocol.serialize(Message.AuthResult.fail("Неверный логин или пароль")));
                }
            }
        }
        return false;
    }

    /**
     * Обрабатывает входящие сообщения после аутентификации.
     */
    private void handleMessages() throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            Message message = Protocol.deserialize(line);

            switch (message) {
                case Message.ChatMessage chat -> {
                    var outgoing = new Message.ChatMessage(login, chat.to(), chat.text());
                    server.sendMessageTo(outgoing);
                    LOG.log(Level.FINE, "{0} -> {1}: {2}",
                            new Object[]{login, chat.to(), chat.text()});
                }
                case Message.Disconnect disconnect -> {
                    LOG.log(Level.INFO, "Пользователь {0} отключается", login);
                    return;
                }
                case null, default -> LOG.log(Level.FINE, "Неизвестное сообщение: {0}", line);
            }
        }
    }

    /**
     * Отправляет сообщение клиенту.
     */
    void send(String json) {
        if (out != null && !socket.isClosed()) {
            out.println(json);
        }
    }

    /**
     * Закрывает соединение и уведомляет сервер.
     */
    private void disconnect() {
        if (login != null) {
            server.unregisterClient(login);
        }

        try {
            socket.close();
        } catch (IOException e) {
            LOG.log(Level.FINE, "Ошибка закрытия сокета: {0}", e.getMessage());
        }
    }
}
