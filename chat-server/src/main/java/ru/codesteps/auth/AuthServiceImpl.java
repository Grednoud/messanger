package ru.codesteps.auth;

import ru.codesteps.persistance.User;
import ru.codesteps.persistance.UserRepository;

import java.sql.SQLException;

public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean authUser(User user) {
        try {
            User userFromDb = userRepository.findByLogin(user.getLogin());
            return userFromDb.getId() > 0 && userFromDb.getPassword().equals(user.getPassword());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
