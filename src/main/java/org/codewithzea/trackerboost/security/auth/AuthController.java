package org.codewithzea.trackerboost.security.auth;



import org.codewithzea.trackerboost.user.dto.UserLoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }



    @PostMapping("/basic-auth-token")
    public ResponseEntity<AuthResponse> basicAuthToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.basicAuthToken(request));
    }

    @GetMapping("/oauth-success")
    public ResponseEntity<AuthResponse> oauthSuccess(
            @RequestParam String token,
            @RequestParam String refreshToken,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant accessTokenExpiry,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant refreshTokenExpiry) {

        return ResponseEntity.ok(new AuthResponse(
                token,
                refreshToken,
                accessTokenExpiry,
                refreshTokenExpiry
        ));
    }
}




















































































//import org.codewithzea.trackerboost.user.UserEntity;
//import org.codewithzea.trackerboost.user.UserRegistrationService;
//import org.codewithzea.trackerboost.user.dto.UserLoginRequest;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
//public class AuthController {
//
//    private final UserRegistrationService registrationService;
//    private final AuthService authService;
//
//
//    @Operation(summary = "Authenticate user and get JWT token")
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Login credentials",
//                    required = true,
//                    content = @Content(
//                            schema = @Schema(implementation = UserLoginRequest.class)
//                    )
//            )
//            @Valid @RequestBody UserLoginRequest request) {
//        return ResponseEntity.ok(authService.login(request));
//    }
//
//    @Operation(summary = "OAuth2 success callback")
//    @GetMapping("/oauth-success")
//    public ResponseEntity<Map<String, String>> oauthSuccess(
//            @RequestParam String token,
//            @AuthenticationPrincipal UserEntity user
//    ) {
//        log.info("OAuth2 authentication successful for user ID: {}", user.getId());
//        return ResponseEntity.ok(Map.of(
//                "token", token,
//                "userId", user.getId().toString(),
//                "role", user.getRole().name()
//        ));
//    }
//
//    @Operation(summary = "Get current user info",
//            security = @SecurityRequirement(name = "bearerAuth"))
//    @GetMapping("/me")
//    public ResponseEntity<UserEntity> getCurrentUser(
//            @AuthenticationPrincipal UserEntity user
//    ) {
//        log.debug("Fetching current user info for ID: {}", user.getId());
//        return ResponseEntity.ok(user);
//    }
//}