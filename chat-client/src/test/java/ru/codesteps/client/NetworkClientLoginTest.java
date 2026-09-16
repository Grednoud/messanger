package ru.codesteps.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.codesteps.protocol.Message;
import ru.codesteps.server.ChatServer;
import ru.codesteps.server.InMemoryAuthService;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * NetworkClientLoginTest проверяет вход через клиентский API.
 */
@DisplayName("NetworkClient login")
class NetworkClientLoginTest {

    private ChatServer server;

    @BeforeEach
    void startServer() throws InterruptedException {
        var auth = new InMemoryAuthService();
        auth.addUser("Alex", "123");
        server = new ChatServer(0, auth);

        Thread.ofVirtual().name("test-chat-server").start(() -> {
            try {
                server.start();
            } catch (IOException ignored) {
                // stopped
            }
        });

        for (int i = 0; i < 100; i++) {
            if (server.getBoundPort() > 0) {
                return;
            }
            Thread.sleep(20);
        }
        fail("Сервер не успел занять порт");
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("Alex/123 can log in")
    void knownUserCanLogIn() throws IOException {
        var received = new CopyOnWriteArrayList<Message>();
        try (var client = new NetworkClient("127.0.0.1", server.getBoundPort(), received::add)) {
            var result = client.connect("Alex", "123");
            assertTrue(result.success(), () -> "ожидался успешный вход, получено: " + result.errorMessage());
            assertTrue(client.isConnected());
        }
    }

    @Test
    @DisplayName("wrong password cannot log in")
    void wrongPasswordCannotLogIn() throws IOException {
        try (var client = new NetworkClient("127.0.0.1", server.getBoundPort(), message -> {
        })) {
            var result = client.connect("Alex", "999");
            assertFalse(result.success());
            assertFalse(client.isConnected());
        }
    }
}
