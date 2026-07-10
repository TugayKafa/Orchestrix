package com.orchestrix.service;

import com.orchestrix.entity.auth.AuthProvider;
import com.orchestrix.entity.auth.Role;
import com.orchestrix.entity.auth.User;
import com.orchestrix.exception.InvalidPasswordException;
import com.orchestrix.exception.UserAlreadyExistsException;
import com.orchestrix.exception.UserNotFoundException;
import com.orchestrix.repository.userRepository.UserRepository;
import com.orchestrix.service.userService.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    UserServiceImpl userService;

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
                        Role.USER, AuthProvider.LOCAL))
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
                         Role.USER, AuthProvider.LOCAL))
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
                        Role.USER, AuthProvider.LOCAL))
                );

        when(passwordEncoder.matches("Password123", "hashed_password"))
                .thenReturn(false);

        assertThrows(
                InvalidPasswordException.class,
                () -> userService.login("ivan@gmail.com", "Password123"),
                "Expected InvalidPasswordException to be thrown."
        );
    }

    @Test
    void testFindByEmailReturnsUser() {
        User existing = new User(
                "ivan@gmail.com", "hash", "Ivan", "Ivanov",
                Role.USER, AuthProvider.LOCAL);
        when(userRepository.findByEmail("ivan@gmail.com"))
                .thenReturn(Optional.of(existing));

        Optional<User> result = userService.findByEmail("ivan@gmail.com");

        assertTrue(
                result.isPresent(),
                "Expected user to be found by email 'ivan@gmail.com'."
        );
        assertEquals(
                "ivan@gmail.com",
                result.get().getEmail(),
                "Expected user's email to be 'ivan@gmail.com'."
        );
    }

    @Test
    void testFindByEmailReturnsEmptyWhenNotFound() {
        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("unknown@gmail.com");

        assertTrue(
                result.isEmpty(),
                "Expected empty result for nonexistent email 'unknown@gmail.com'."
        );
    }

    @Test
    void testCreateOAuthUserWithNullPassword() {
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.createOAuthUser(
                "ivan@gmail.com", "Ivan", "Ivanov", AuthProvider.GOOGLE);

        assertEquals(
                "ivan@gmail.com",
                user.getEmail(),
                "Expected OAuth user's email to be 'ivan@gmail.com'."
        );
        assertEquals(
                "Ivan",
                user.getFirstName(),
                "Expected OAuth user's first name to be 'Ivan'."
        );
        assertEquals(
                "Ivanov",
                user.getLastName(),
                "Expected OAuth user's last name to be 'Ivanov'."
        );
        assertEquals(
                AuthProvider.GOOGLE,
                user.getAuthProvider(),
                "Expected OAuth user's auth provider to be GOOGLE."
        );
        assertNull(
                user.getPasswordHash(),
                "Expected OAuth user's password to be null."
        );
        verify(userRepository).save(any(User.class));
    }
}
