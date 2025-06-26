package org.codewithzea.trackerboost.security.auth;


import jakarta.servlet.http.HttpServletRequest;
import org.codewithzea.trackerboost.security.JwtService;
import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.UserRepository;
import org.codewithzea.trackerboost.user.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(UserLoginRequest request) {
        // Manual authentication with password check
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return generateAuthResponse(user);
    }


    public AuthResponse basicAuthToken(HttpServletRequest request) {
        // Extract Basic Auth header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            throw new BadCredentialsException("Missing or invalid Authorization header");
        }

        // Decode credentials
        String base64Credentials = authorizationHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            throw new BadCredentialsException("Invalid Basic Auth format");
        }

        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(values[0], values[1])
        );

        // Get user and generate tokens
        UserEntity user = userRepository.findByEmail(values[0])
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(UserEntity user) {
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        Instant accessExpiry = jwtService.getExpirationFromToken(jwtToken);
        Instant refreshExpiry = jwtService.getExpirationFromToken(refreshToken);

        return new AuthResponse(jwtToken, refreshToken, accessExpiry, refreshExpiry);
    }
}