package com.crosspay.user.controller;

import com.crosspay.user.dto.UserResponse;
import com.crosspay.user.entity.User;
import com.crosspay.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Authenticated user profile operations.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the profile represented by the authenticated JWT.")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {

        UUID userId = (UUID) authentication.getPrincipal();

        return userService.findById(userId)
                .map(this::toUserResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UserResponse toUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCountry(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
