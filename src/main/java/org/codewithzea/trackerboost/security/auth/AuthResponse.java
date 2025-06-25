package org.codewithzea.trackerboost.security.auth;

import java.time.Instant;

public record AuthResponse(String token, String refreshToken, Instant accessTokenExpiry,
                           Instant refreshTokenExpiry) {
}