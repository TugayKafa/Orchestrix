package com.orchestrix.service;

import com.orchestrix.entity.Role;
import com.orchestrix.entity.User;
import com.orchestrix.exception.InvalidPasswordException;
import com.orchestrix.exception.UserAlreadyExistsException;
import com.orchestrix.exception.UserNotFoundException;
import com.orchestrix.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void testValidRegistration() {
        when(userRepository.findByEmail("ivan@gmail.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123"))
                .thenReturn("hashed_password");

        User user = userService.register(
                "ivan@gmail.com",
                "Password123",
                "Ivan",
                "Ivanov"
        );
        verify(userRepository).save(any(User.class));

        assertEquals(
                "ivan@gmail.com",
                user.getEmail(),
                "Expected user's email to be 'ivan@gmail.com'."
        );
        assertEquals(
                "Ivan",
                user.getFirstName(),
                "Expected user's first name to be 'Ivan'."
        );
        assertEquals(
                "Ivanov",
                user.getLastName(),
                "Expected user's last name to be 'Ivanov'."
        );
        assertEquals(
                "hashed_password",
                user.getPasswordHash(),
                "Expected user's password to be hashed to 'hashed_password'."
        );
    }

    @Test
    void testRegisterWithExistingEmailThrowsException() {
        when(userRepository.findByEmail("ivan@gmail.com"))
                .thenReturn(Optional.of(new User(
                        "ivan@gmail.com",
                        "hash",
                        "Ivan",
                        "Ivanov",
                        Role.USER))
                );

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(
                "ivan@gmail.com",
                "Password123",
                "3333",
                "Ivanov"),
                "Expected UserAlreadyExistsException to be thrown."
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void testValidLogin() {
        when(userRepository.findByEmail("ivan@gmail.com"))
                .thenReturn(Optional.of(new User(
                        "ivan@gmail.com",
                        "hashed_password",
                        "Ivan",
                         "Ivanov",
                         Role.USER))
                );
        when(passwordEncoder.matches("Password123", "hashed_password"))
                .thenReturn(true);

        User user =  userService.login(
                "ivan@gmail.com",
                "Password123"
        );

        assertEquals(
                "ivan@gmail.com",
                user.getEmail(),
                "Expected user's email to be 'ivan@gmail.com'."
        );
        assertEquals(
                "Ivan",
                user.getFirstName(),
                "Expected user's first name to be 'Ivan'."
        );
    }

    @Test
    void testLoginWithInvalidEmailThrowsException() {
        when(userRepository.findByEmail("ivan@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.login("ivan@gmail.com", "Password123"),
                "Expected UserNotFoundException to be thrown."
        );

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testLoginWithInvalidPasswordThrowsException() {
        when(userRepository.findByEmail("ivan@gmail.com"))
                .thenReturn(Optional.of(new User(
                        "ivan@gmail.com",
                        "hashed_password",
                        "Ivan",
                        "Ivanov",
                        Role.USER))
                );

        when(passwordEncoder.matches("Password123", "hashed_password"))
                .thenReturn(false);

        assertThrows(
                InvalidPasswordException.class,
                () -> userService.login("ivan@gmail.com", "Password123"),
                "Expected InvalidPasswordException to be thrown."
        );
    }
}
