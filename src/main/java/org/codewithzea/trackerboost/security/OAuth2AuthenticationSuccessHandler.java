package org.codewithzea.trackerboost.security;



import org.codewithzea.trackerboost.security.auth.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        String token = jwtService.generateToken(oauthUser.getUserEntity());


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format("{\"token\":\"%s\",\"role\":\"%s\"}",
                        token,
                        oauthUser.getUserEntity().getRole().name())
        );
        response.getWriter().flush();

    }
}
