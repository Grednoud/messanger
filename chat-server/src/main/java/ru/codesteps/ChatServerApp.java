package ru.codesteps;

import ru.codesteps.auth.AuthService;
import ru.codesteps.auth.AuthServiceImpl;
import ru.codesteps.persistance.User;
import ru.codesteps.persistance.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ChatServerApp {
    public static final int PORT = 11111;

    public static void main(String[] args) {
        AuthService authService;
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/chat", "postgres", "postgres");
            UserRepository userRepository = new UserRepository(connection);
            if (userRepository.getAllUsers().size() == 0) {
                userRepository.insert(new User(-1, "Alex", "123"));
                userRepository.insert(new User(-1, "Bob", "234"));
                userRepository.insert(new User(-1, "Clod", "345"));
            }
            authService = new AuthServiceImpl(userRepository);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        Server server = new Server(PORT);
        server.setAuthService(authService);
        server.start();
    }
}
