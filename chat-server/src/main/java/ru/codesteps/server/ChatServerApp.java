package ru.codesteps.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ChatServerApp — точка входа сервера чата.
 * Запускает сервер на указанном порту (по умолчанию 11111).
 */
public final class ChatServerApp {

    private static final Logger LOG = Logger.getLogger(ChatServerApp.class.getName());
    private static final int DEFAULT_PORT = 11111;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                LOG.log(Level.WARNING, "Некорректный порт: {0}, используется {1}",
                        new Object[]{args[0], DEFAULT_PORT});
            }
        }

        var authService = new InMemoryAuthService();
        authService.addUser("Alex", "123");
        authService.addUser("Bob", "234");
        authService.addUser("Clod", "345");

        var server = new ChatServer(port, authService);

        try {
            server.start();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Не удалось запустить сервер: {0}", e.getMessage());
        }
    }
}
