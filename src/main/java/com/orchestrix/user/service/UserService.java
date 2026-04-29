package com.orchestrix.user.service;

import com.orchestrix.user.entity.Role;
import com.orchestrix.user.entity.User;
import com.orchestrix.user.exception.UserAlreadyExistsException;
import com.orchestrix.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public void register(String email, String password, String name) {
        if (users.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email: " + email + " is already in use.");
        }

        users.save(new User(email, encoder.encode(password), name, Role.USER));
    }
}
