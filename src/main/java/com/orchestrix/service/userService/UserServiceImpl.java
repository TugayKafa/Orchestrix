package com.orchestrix.service.userService;

import com.orchestrix.entity.auth.AuthProvider;
import com.orchestrix.entity.auth.Role;
import com.orchestrix.entity.auth.User;
import com.orchestrix.exception.InvalidPasswordException;
import com.orchestrix.exception.UserAlreadyExistsException;
import com.orchestrix.exception.UserNotFoundException;
import com.orchestrix.repository.userRepository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(String email, String password, String firstName, String lastName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(String.format("Email: %s is already in use.", email));
        }

        User user = new User(email, passwordEncoder.encode(password),
                firstName, lastName, Role.USER, AuthProvider.LOCAL);
        userRepository.save(user);
        return user;
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidPasswordException("Invalid credentials.");
        }

        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createOAuthUser(String email, String firstName, String lastName, AuthProvider provider) {
        User user = new User(email, null, firstName, lastName, Role.USER, provider);
        return userRepository.save(user);
    }
}
