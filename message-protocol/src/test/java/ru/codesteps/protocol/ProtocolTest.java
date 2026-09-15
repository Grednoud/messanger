package ru.codesteps.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProtocolTest тестирует сериализацию и десериализацию сообщений протокола.
 */
@DisplayName("Protocol")
class ProtocolTest {

    @Nested
    @DisplayName("Auth message")
    class AuthMessageTest {

        @Test
        @DisplayName("should serialize and deserialize auth request")
        void serializeAndDeserialize() {
            var original = new Message.Auth("user1", "secret123");
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"AUTH\""));
            assertTrue(json.contains("\"login\":\"user1\""));
            assertTrue(json.contains("\"password\":\"secret123\""));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.Auth.class, result);
            var auth = (Message.Auth) result;
            assertEquals("user1", auth.login());
            assertEquals("secret123", auth.password());
        }
    }

    @Nested
    @DisplayName("AuthResult message")
    class AuthResultTest {

        @Test
        @DisplayName("should serialize success result")
        void serializeSuccess() {
            var original = Message.AuthResult.ok();
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"successful\":true"));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.AuthResult.class, result);
            var authResult = (Message.AuthResult) result;
            assertTrue(authResult.success());
        }

        @Test
        @DisplayName("should serialize failure result with message")
        void serializeFailure() {
            var original = Message.AuthResult.fail("Invalid credentials");
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"successful\":false"));
            assertTrue(json.contains("Invalid credentials"));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.AuthResult.class, result);
            var authResult = (Message.AuthResult) result;
            assertFalse(authResult.success());
            assertEquals("Invalid credentials", authResult.message());
        }
    }

    @Nested
    @DisplayName("ChatMessage")
    class ChatMessageTest {

        @Test
        @DisplayName("should serialize and deserialize chat message")
        void serializeAndDeserialize() {
            var timestamp = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
            var original = new Message.ChatMessage("alice", "bob", "Hello!", timestamp);
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"CHAT\""));
            assertTrue(json.contains("\"from\":\"alice\""));
            assertTrue(json.contains("\"to\":\"bob\""));
            assertTrue(json.contains("\"text\":\"Hello!\""));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.ChatMessage.class, result);
            var chat = (Message.ChatMessage) result;
            assertEquals("alice", chat.from());
            assertEquals("bob", chat.to());
            assertEquals("Hello!", chat.text());
            assertEquals(timestamp, chat.timestamp());
        }

        @Test
        @DisplayName("should create message with current timestamp")
        void createWithCurrentTimestamp() {
            var before = LocalDateTime.now();
            var message = new Message.ChatMessage("alice", "bob", "Test");
            var after = LocalDateTime.now();

            assertNotNull(message.timestamp());
            assertFalse(message.timestamp().isBefore(before));
            assertFalse(message.timestamp().isAfter(after));
        }
    }

    @Nested
    @DisplayName("UserConnected message")
    class UserConnectedTest {

        @Test
        @DisplayName("should serialize and deserialize")
        void serializeAndDeserialize() {
            var original = new Message.UserConnected("newuser");
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"USER_CONNECTED\""));
            assertTrue(json.contains("\"login\":\"newuser\""));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.UserConnected.class, result);
            assertEquals("newuser", ((Message.UserConnected) result).login());
        }
    }

    @Nested
    @DisplayName("UserDisconnected message")
    class UserDisconnectedTest {

        @Test
        @DisplayName("should serialize and deserialize")
        void serializeAndDeserialize() {
            var original = new Message.UserDisconnected("leftuser");
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"USER_DISCONNECTED\""));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.UserDisconnected.class, result);
            assertEquals("leftuser", ((Message.UserDisconnected) result).login());
        }
    }

    @Nested
    @DisplayName("UserList message")
    class UserListTest {

        @Test
        @DisplayName("should serialize and deserialize user list")
        void serializeAndDeserialize() {
            var original = new Message.UserList(List.of("alice", "bob", "charlie"));
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"USER_LIST\""));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.UserList.class, result);
            var userList = (Message.UserList) result;
            assertEquals(3, userList.users().size());
            assertTrue(userList.users().containsAll(List.of("alice", "bob", "charlie")));
        }

        @Test
        @DisplayName("should handle empty list")
        void handleEmptyList() {
            var original = new Message.UserList(List.of());
            String json = Protocol.serialize(original);

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.UserList.class, result);
            assertTrue(((Message.UserList) result).users().isEmpty());
        }
    }

    @Nested
    @DisplayName("Disconnect message")
    class DisconnectTest {

        @Test
        @DisplayName("should serialize and deserialize")
        void serializeAndDeserialize() {
            var original = new Message.Disconnect();
            String json = Protocol.serialize(original);

            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"DISCONNECT\""));

            Message result = Protocol.deserialize(json);

            assertInstanceOf(Message.Disconnect.class, result);
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTest {

        @Test
        @DisplayName("should return null for null input")
        void handleNull() {
            assertNull(Protocol.deserialize(null));
        }

        @Test
        @DisplayName("should return null for empty string")
        void handleEmpty() {
            assertNull(Protocol.deserialize(""));
            assertNull(Protocol.deserialize("   "));
        }

        @Test
        @DisplayName("should return null for invalid JSON")
        void handleInvalidJson() {
            assertNull(Protocol.deserialize("not json"));
            assertNull(Protocol.deserialize("{broken"));
        }

        @Test
        @DisplayName("should return null for unknown message type")
        void handleUnknownType() {
            assertNull(Protocol.deserialize("{\"type\":\"UNKNOWN\",\"data\":{}}"));
        }
    }
}
