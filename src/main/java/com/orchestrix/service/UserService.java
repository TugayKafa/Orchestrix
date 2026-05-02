package com.orchestrix.service;

import com.orchestrix.entity.Role;
import com.orchestrix.entity.User;
import com.orchestrix.exception.InvalidPasswordException;
import com.orchestrix.exception.UserAlreadyExistsException;
import com.orchestrix.exception.UserNotFoundException;
import com.orchestrix.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User register(String email, String password, String firstName, String lastName) {
        if (users.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email: " + email + " is already in use.");
        }

        User user = new User(email, encoder.encode(password), firstName, lastName, Role.USER);
        users.save(user);
        return user;
    }

    public User login(String email, String password) {
        Optional<User> user = users.findByEmail(email);

        if (user.isEmpty()) {
            throw new UserNotFoundException("Invalid credentials.");
        }

        if (!encoder.matches(password, user.get().getPasswordHash())) {
            throw new InvalidPasswordException("Invalid credentials.");
        }

        return user.get();
    }
}
