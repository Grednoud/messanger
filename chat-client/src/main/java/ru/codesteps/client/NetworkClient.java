package ru.codesteps.client;

import ru.codesteps.protocol.Message;
import ru.codesteps.protocol.Protocol;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NetworkClient управляет сетевым соединением с сервером чата.
 */
public final class NetworkClient implements Closeable {

    private static final Logger LOG = Logger.getLogger(NetworkClient.class.getName());

    private final String host;
    private final int port;
    private final Consumer<Message> messageHandler;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread receiverThread;
    private String login;
    private volatile boolean connected;

    public NetworkClient(String host, int port, Consumer<Message> messageHandler) {
        this.host = host;
        this.port = port;
        this.messageHandler = messageHandler;
    }

    /**
     * Подключается к серверу и выполняет аутентификацию.
     *
     * @param login    логин
     * @param password пароль
     * @return результат аутентификации
     * @throws IOException при ошибке сети
     */
    public AuthResult connect(String login, String password) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        send(new Message.Auth(login, password));

        var pending = new java.util.ArrayList<Message>();
        String response;
        while ((response = in.readLine()) != null) {
            Message message = Protocol.deserialize(response);

            if (message instanceof Message.AuthResult result) {
                if (result.success()) {
                    this.login = login;
                    this.connected = true;
                    startReceiverThread();
                    pending.forEach(messageHandler);
                    return new AuthResult(true, null);
                }
                socket.close();
                return new AuthResult(false, result.message());
            }

            if (message != null) {
                pending.add(message);
            }
        }

        socket.close();
        return new AuthResult(false, "Сервер закрыл соединение");
    }

    /**
     * Отправляет сообщение пользователю.
     */
    public void sendMessage(String to, String text) {
        if (connected && login != null) {
            send(new Message.ChatMessage(login, to, text));
        }
    }

    /**
     * Отправляет сообщение на сервер.
     */
    private void send(Message message) {
        if (out != null) {
            out.println(Protocol.serialize(message));
        }
    }

    /**
     * Запускает поток приёма сообщений.
     */
    private void startReceiverThread() {
        receiverThread = Thread.ofVirtual().name("message-receiver").start(() -> {
            try {
                String line;
                while (connected && (line = in.readLine()) != null) {
                    Message message = Protocol.deserialize(line);
                    if (message != null) {
                        messageHandler.accept(message);
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    LOG.log(Level.WARNING, "Соединение прервано: {0}", e.getMessage());
                    connected = false;
                }
            }
        });
    }

    public String getLogin() {
        return login;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        connected = false;

        if (out != null) {
            send(new Message.Disconnect());
        }

        try {
            if (receiverThread != null) {
                receiverThread.interrupt();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LOG.log(Level.FINE, "Ошибка закрытия соединения: {0}", e.getMessage());
        }
    }

    /**
     * Результат попытки аутентификации.
     */
    public record AuthResult(boolean success, String errorMessage) {
    }
}
