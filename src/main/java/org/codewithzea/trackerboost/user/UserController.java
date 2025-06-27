package org.codewithzea.trackerboost.user;



import org.codewithzea.trackerboost.security.auth.AuthResponse;
import org.codewithzea.trackerboost.security.auth.AuthService;
import org.codewithzea.trackerboost.user.dto.UserLoginRequest;
import org.codewithzea.trackerboost.user.dto.UserRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class UserController {

    private final UserRegistrationService registrationService;
    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(
            @Valid @RequestBody UserRegistrationRequest request
    ) {
        log.info("Registration attempt for email: {}", request.email());
        UserEntity newUser = registrationService.registerUser(request);
        log.info("User registered successfully with ID: {}", newUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newUser);
    }

    @Operation(summary = "Authenticate user and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        log.info("Login attempt for email: {}", request.email());
        AuthResponse response = authService.login(request);
        log.debug("Login successful for email: {}", request.email());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "OAuth2 success callback")
    @GetMapping("/oauth-success")
    public ResponseEntity<Map<String, String>> oauthSuccess(
            @RequestParam String token,
            @AuthenticationPrincipal UserEntity user
    ) {
        log.info("OAuth2 authentication successful for user ID: {}", user.getId());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId().toString(),
                "role", user.getRole().name()
        ));
    }

    @Operation(summary = "Get current user info",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal UserEntity user
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        log.debug("Fetching current user info for ID: {}", user.getId());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user tasks",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}/tasks")
    public ResponseEntity<?> getUserTasks(
            @PathVariable Long userId,
            @AuthenticationPrincipal(expression = "userEntity") UserEntity currentUser) {

        // Check authentication
        if (currentUser == null) {
            log.warn("Unauthenticated access attempt to user tasks endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }


        if (!currentUser.getId().equals(userId)) {
            log.warn("Unauthorized access attempt by user {} to tasks of user {}",
                    currentUser.getId(), userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }

        // Process request
        try {
            log.debug("Fetching tasks for user {}", userId);
            return ResponseEntity.ok(userService.getUserTasks(userId));
        } catch (Exception e) {
            log.error("Error fetching tasks for user {}", userId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch tasks"));
        }
    }
}