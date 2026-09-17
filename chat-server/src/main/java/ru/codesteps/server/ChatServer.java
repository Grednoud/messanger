package ru.codesteps.server;

import ru.codesteps.protocol.Message;
import ru.codesteps.protocol.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatServer управляет подключениями клиентов и маршрутизацией сообщений.
 */
public final class ChatServer {

    private static final Logger LOG = Logger.getLogger(ChatServer.class.getName());

    private final int port;
    private final AuthService authService;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private volatile boolean running;
    private ServerSocket serverSocket;

    public ChatServer(int port, AuthService authService) {
        this.port = port;
        this.authService = authService;
    }

    /**
     * Запускает сервер и принимает входящие соединения.
     */
    public void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);
        LOG.log(Level.INFO, "Сервер запущен на порту {0}", getBoundPort());

        try {
            while (running) {
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    if (!running) {
                        break;
                    }
                    throw e;
                }
                LOG.log(Level.INFO, "Новое подключение: {0}", clientSocket.getRemoteSocketAddress());
                executor.submit(new ClientHandler(this, clientSocket, authService));
            }
        } finally {
            closeServerSocket();
        }
    }

    /**
     * Порт, на котором сервер слушает соединения, или 0 если ещё не запущен.
     */
    public int getBoundPort() {
        ServerSocket socket = serverSocket;
        return socket != null && socket.isBound() && !socket.isClosed()
                ? socket.getLocalPort()
                : 0;
    }

    /**
     * Останавливает сервер.
     */
    public void stop() {
        running = false;
        closeServerSocket();
        executor.shutdown();
    }

    private void closeServerSocket() {
        ServerSocket socket = serverSocket;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // already stopping
            }
        }
    }

    /**
     * Регистрирует подключённого клиента.
     */
    void registerClient(String login, ClientHandler handler) {
        clients.put(login, handler);
        broadcastUserConnected(login);
        sendUserList(handler);
        LOG.log(Level.INFO, "Пользователь {0} подключён. Всего: {1}",
                new Object[]{login, clients.size()});
    }

    /**
     * Удаляет отключившегося клиента.
     */
    void unregisterClient(String login) {
        if (clients.remove(login) != null) {
            broadcastUserDisconnected(login);
            LOG.log(Level.INFO, "Пользователь {0} отключён. Всего: {1}",
                    new Object[]{login, clients.size()});
        }
    }

    /**
     * Проверяет, подключён ли пользователь.
     */
    boolean isUserOnline(String login) {
        return clients.containsKey(login);
    }

    /**
     * Отправляет личное сообщение указанному пользователю.
     */
    void sendMessageTo(Message.ChatMessage message) {
        ClientHandler recipient = clients.get(message.to());
        if (recipient != null) {
            recipient.send(Protocol.serialize(message));
        } else {
            LOG.log(Level.FINE, "Пользователь {0} не в сети", message.to());
        }
    }

    private void broadcastUserConnected(String login) {
        String json = Protocol.serialize(new Message.UserConnected(login));
        clients.forEach((name, handler) -> {
            if (!name.equals(login)) {
                handler.send(json);
            }
        });
    }

    private void broadcastUserDisconnected(String login) {
        String json = Protocol.serialize(new Message.UserDisconnected(login));
        clients.forEach((name, handler) -> handler.send(json));
    }

    private void sendUserList(ClientHandler handler) {
        List<String> users = List.copyOf(clients.keySet());
        handler.send(Protocol.serialize(new Message.UserList(users)));
    }
}
