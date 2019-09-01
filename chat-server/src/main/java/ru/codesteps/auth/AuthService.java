package ru.codesteps.auth;

import ru.codesteps.persistance.User;

public interface AuthService {
    boolean authUser(User user);
}
