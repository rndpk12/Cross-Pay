package com.crosspay.auth.controller;

import com.crosspay.auth.dto.LoginRequest;
import com.crosspay.auth.dto.LoginResponse;
import com.crosspay.auth.dto.RegisterRequest;
import com.crosspay.auth.dto.RegisterResponse;
import com.crosspay.auth.service.JwtService;
import com.crosspay.user.entity.User;
import com.crosspay.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Public registration and JWT login endpoints")
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
    @Operation(summary = "Register a user", description = "Creates a new Cross Pay user. This endpoint does not require a bearer token.")
    @SecurityRequirements
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
    @Operation(summary = "Log in", description = "Authenticates a user and returns a Bearer JWT for protected endpoints.")
    @SecurityRequirements
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
