package ru.codesteps.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InMemoryAuthServiceTest тестирует сервис аутентификации.
 */
@DisplayName("InMemoryAuthService")
class InMemoryAuthServiceTest {

    private InMemoryAuthService authService;

    @BeforeEach
    void setUp() {
        authService = new InMemoryAuthService();
    }

    @Nested
    @DisplayName("authenticate")
    class AuthenticateTest {

        @Test
        @DisplayName("should return true for valid credentials")
        void validCredentials() {
            authService.addUser("alice", "password123");

            assertTrue(authService.authenticate("alice", "password123"));
        }

        @Test
        @DisplayName("should return false for wrong password")
        void wrongPassword() {
            authService.addUser("alice", "password123");

            assertFalse(authService.authenticate("alice", "wrongpassword"));
        }

        @Test
        @DisplayName("should return false for non-existent user")
        void nonExistentUser() {
            assertFalse(authService.authenticate("unknown", "password"));
        }

        @Test
        @DisplayName("should return false for null login")
        void nullLogin() {
            authService.addUser("alice", "password123");

            assertFalse(authService.authenticate(null, "password123"));
        }

        @Test
        @DisplayName("should return false for null password")
        void nullPassword() {
            authService.addUser("alice", "password123");

            assertFalse(authService.authenticate("alice", null));
        }

        @Test
        @DisplayName("should be case-sensitive for login")
        void caseSensitiveLogin() {
            authService.addUser("Alice", "password123");

            assertTrue(authService.authenticate("Alice", "password123"));
            assertFalse(authService.authenticate("alice", "password123"));
            assertFalse(authService.authenticate("ALICE", "password123"));
        }

        @Test
        @DisplayName("should be case-sensitive for password")
        void caseSensitivePassword() {
            authService.addUser("alice", "Password123");

            assertTrue(authService.authenticate("alice", "Password123"));
            assertFalse(authService.authenticate("alice", "password123"));
        }
    }

    @Nested
    @DisplayName("userExists")
    class UserExistsTest {

        @Test
        @DisplayName("should return true for existing user")
        void existingUser() {
            authService.addUser("bob", "secret");

            assertTrue(authService.userExists("bob"));
        }

        @Test
        @DisplayName("should return false for non-existent user")
        void nonExistentUser() {
            assertFalse(authService.userExists("unknown"));
        }

        @Test
        @DisplayName("should return false for null login")
        void nullLogin() {
            authService.addUser("bob", "secret");

            assertFalse(authService.userExists(null));
        }
    }

    @Nested
    @DisplayName("addUser")
    class AddUserTest {

        @Test
        @DisplayName("should allow adding multiple users")
        void multipleUsers() {
            authService.addUser("user1", "pass1");
            authService.addUser("user2", "pass2");
            authService.addUser("user3", "pass3");

            assertTrue(authService.authenticate("user1", "pass1"));
            assertTrue(authService.authenticate("user2", "pass2"));
            assertTrue(authService.authenticate("user3", "pass3"));
        }

        @Test
        @DisplayName("should overwrite existing user password")
        void overwritePassword() {
            authService.addUser("alice", "oldpassword");
            authService.addUser("alice", "newpassword");

            assertFalse(authService.authenticate("alice", "oldpassword"));
            assertTrue(authService.authenticate("alice", "newpassword"));
        }
    }
}
