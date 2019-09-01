package ru.codesteps.persistance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final Connection connection;

    public UserRepository(Connection connection) throws SQLException {
        this.connection = connection;
        createTableIfNotExists(connection);
    }

    private void createTableIfNotExists(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS public.users\n" +
                    "(\n" +
                    "    id serial NOT NULL,\n" +
                    "    login character varying(25) NOT NULL,\n" +
                    "    password character varying(25) NOT NULL,\n" +
                    "    PRIMARY KEY (id)\n" +
                    ")\n" +
                    "WITH (\n" +
                    "    OIDS = FALSE\n" +
                    ");");
        }
    }

    public void insert(User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO users(login, password) VALUES(?, ?)")) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPassword());
            stmt.execute();
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> res = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, login, password FROM users");
            while (rs.next()) {
                res.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return res;
    }

    public User findByLogin(String login) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE login = ?")) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        }
        return new User(-1, "", "");
    }
}
