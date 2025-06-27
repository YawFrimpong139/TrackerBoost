package org.codewithzea.trackerboost.security;



import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.codewithzea.trackerboost.security.auth.AuthResponse;
import org.codewithzea.trackerboost.security.auth.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;


@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

        // Generate tokens once
        String token = jwtService.generateToken(oauthUser.getUserEntity());
        String refreshToken = jwtService.generateRefreshToken(oauthUser.getUserEntity());

        // Set tokens in cookies (for browser access)
        addTokenCookies(request, response, token, refreshToken);

        // Set tokens in headers (for API clients)
        response.setHeader("Authorization", "Bearer " + token);
        response.setHeader("X-Refresh-Token", refreshToken);

        // Return JSON response
        AuthResponse authResponse = new AuthResponse(
                token,
                refreshToken,
                jwtService.getExpirationFromToken(token),
                jwtService.getExpirationFromToken(refreshToken)
        );

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }

    private void addTokenCookies(HttpServletRequest request, HttpServletResponse response, String token, String refreshToken) {
        // Access token cookie
        Cookie accessCookie = new Cookie("access_token", token);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(request.isSecure()); // Auto-detect HTTPS
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) Duration.ofHours(1).toSeconds());
        response.addCookie(accessCookie);

        // Refresh token cookie
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(request.isSecure());
        refreshCookie.setPath("/auth/refresh");
        refreshCookie.setMaxAge((int) Duration.ofDays(7).toSeconds());
        response.addCookie(refreshCookie);
    }
}
