package ru.codesteps.server;

/**
 * AuthService — интерфейс сервиса аутентификации.
 */
public interface AuthService {

    /**
     * Проверяет учётные данные пользователя.
     *
     * @param login    логин
     * @param password пароль
     * @return true если аутентификация успешна
     */
    boolean authenticate(String login, String password);

    /**
     * Проверяет, существует ли пользователь с указанным логином.
     *
     * @param login логин
     * @return true если пользователь существует
     */
    boolean userExists(String login);
}
