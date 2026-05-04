package com.orchestrix.service;

import com.orchestrix.entity.AuthProvider;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String password, String firstName, String lastName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(String.format("Email: %s is already in use.", email));
        }

        User user = new User(email, passwordEncoder.encode(password), firstName, lastName, Role.USER, AuthProvider.LOCAL);
        userRepository.save(user);
        return user;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidPasswordException("Invalid credentials.");
        }

        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createOAuthUser(String email, String firstName, String lastName, AuthProvider provider) {
        User user = new User(email, null, firstName, lastName, Role.USER, provider);
        return userRepository.save(user);
    }
}
