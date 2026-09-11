package com.crosspay.auth.controller;

import com.crosspay.auth.dto.LoginRequest;
import com.crosspay.auth.dto.LoginResponse;
import com.crosspay.auth.dto.RegisterRequest;
import com.crosspay.auth.dto.RegisterResponse;
import com.crosspay.auth.service.JwtService;
import com.crosspay.user.entity.User;
import com.crosspay.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        User user = userService.register(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCountry(),
                user.getStatus()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        User user = userService.authenticate(request);

        String accessToken = jwtService.generateAccessToken(user);

        LoginResponse response = new LoginResponse(
                accessToken,
                "Bearer"
        );

        return ResponseEntity.ok(response);
    }
}