package ru.codesteps.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.codesteps.protocol.Message;
import ru.codesteps.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * LoginIntegrationTest проверяет, что сервер отвечает AUTH_RESULT
 * первым сообщением после запроса логина.
 */
@DisplayName("Chat login")
class LoginIntegrationTest {

    private ChatServer server;

    @BeforeEach
    void startServer() throws InterruptedException {
        var auth = new InMemoryAuthService();
        auth.addUser("Alex", "123");
        auth.addUser("Bob", "234");
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
    @DisplayName("successful login returns AUTH_RESULT first")
    void successfulLoginReturnsAuthResultFirst() throws IOException {
        try (var session = connect()) {
            session.send(new Message.Auth("Alex", "123"));

            Message first = session.read();
            assertInstanceOf(Message.AuthResult.class, first, "первым должен быть AUTH_RESULT, а не " + first);
            assertTrue(((Message.AuthResult) first).success());
        }
    }

    @Test
    @DisplayName("wrong password is rejected")
    void wrongPasswordIsRejected() throws IOException {
        try (var session = connect()) {
            session.send(new Message.Auth("Alex", "wrong"));

            Message first = session.read();
            assertInstanceOf(Message.AuthResult.class, first);
            var result = (Message.AuthResult) first;
            assertFalse(result.success());
            assertEquals("Неверный логин или пароль", result.message());
        }
    }

    @Test
    @DisplayName("unknown user is rejected")
    void unknownUserIsRejected() throws IOException {
        try (var session = connect()) {
            session.send(new Message.Auth("Nobody", "123"));

            Message first = session.read();
            assertInstanceOf(Message.AuthResult.class, first);
            assertFalse(((Message.AuthResult) first).success());
        }
    }

    private TestSession connect() throws IOException {
        return new TestSession(new Socket("127.0.0.1", server.getBoundPort()));
    }

    private static final class TestSession implements AutoCloseable {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;

        private TestSession(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }

        void send(Message message) {
            out.println(Protocol.serialize(message));
        }

        Message read() throws IOException {
            return Protocol.deserialize(in.readLine());
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
