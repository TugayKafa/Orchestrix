package com.orchestrix.service.userService;

import com.orchestrix.entity.auth.AuthProvider;
import com.orchestrix.entity.auth.User;

import java.util.Optional;

public interface UserService {

    User register(String email, String password, String firstName, String lastName);

    User login(String email, String password);

    Optional<User> findByEmail(String email);

    User createOAuthUser(String email, String firstName, String lastName, AuthProvider provider);
}
