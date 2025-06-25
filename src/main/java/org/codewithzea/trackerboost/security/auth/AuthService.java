package org.codewithzea.trackerboost.security.auth;


import org.codewithzea.trackerboost.security.JwtService;
import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.UserRepository;
import org.codewithzea.trackerboost.user.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Assuming you have JWT implementation

    // In your authentication service
    public AuthResponse login(UserLoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Instant accessExpiry = jwtService.getExpirationFromToken(jwtToken);
        Instant refreshExpiry = jwtService.getExpirationFromToken(refreshToken);


        return new AuthResponse(jwtToken, refreshToken, accessExpiry, refreshExpiry);
    }
}