package com.orchestrix.user.controller;

import com.orchestrix.user.dto.RegisterRequest;
import com.orchestrix.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public void register(@RequestBody @Valid RegisterRequest request) {
        service.register(request.email(), request.password(), request.name());
    }
}
