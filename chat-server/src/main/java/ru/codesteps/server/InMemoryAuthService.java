package ru.codesteps.server;

import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryAuthService — простая реализация AuthService с хранением в памяти.
 * Для учебных целей — пароли хранятся в открытом виде.
 */
public final class InMemoryAuthService implements AuthService {

    private final ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();

    /**
     * Добавляет пользователя.
     *
     * @param login    логин
     * @param password пароль
     */
    public void addUser(String login, String password) {
        users.put(login, password);
    }

    @Override
    public boolean authenticate(String login, String password) {
        if (login == null || password == null) {
            return false;
        }
        String storedPassword = users.get(login);
        return storedPassword != null && storedPassword.equals(password);
    }

    @Override
    public boolean userExists(String login) {
        return login != null && users.containsKey(login);
    }
}
