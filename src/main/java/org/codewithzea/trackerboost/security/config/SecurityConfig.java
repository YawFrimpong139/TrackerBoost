package org.codewithzea.trackerboost.security.config;


import org.codewithzea.trackerboost.audit.SecurityAuditService;
import org.codewithzea.trackerboost.security.CustomOAuth2UserService;
import org.codewithzea.trackerboost.security.JwtFilter;
import org.codewithzea.trackerboost.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final SecurityAuditService securityAuditService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/api/v1/user/register/**",
                                "/error",
                                "/swagger-ui/**",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/h2-console/**").hasRole("ADMIN")

                        // Project endpoints
                        .requestMatchers(HttpMethod.POST, "/api/projects").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/projects/summary").hasAnyRole("ADMIN", "MANAGER", "DEVELOPER", "CONTRACTOR")
                        .requestMatchers(HttpMethod.GET, "/api/projects/**").hasAnyRole("ADMIN", "MANAGER", "DEVELOPER")

                        // Task endpoints
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/**").hasRole("DEVELOPER")
                        .requestMatchers(HttpMethod.GET, "/api/tasks/**").hasAnyRole("ADMIN", "MANAGER", "DEVELOPER")

                        // User management
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                // OAuth2 Login Configuration
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization") // Authorization endpoint
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*") // Callback endpoint
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            // Log successful form login
                            securityAuditService.logLoginSuccess(
                                    authentication.getName(),
                                    request.getRemoteAddr()
                            );
                            response.sendRedirect("/");
                        })
                        .failureHandler((request, response, exception) -> {
                            // Log failed form login
                            String username = request.getParameter("username");
                            securityAuditService.logLoginFailure(
                                    username,
                                    request.getRemoteAddr(),
                                    exception.getMessage()
                            );
                            response.sendRedirect("/login?error");
                        })
                        .permitAll()
                )
                // JWT Filter Configuration
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );


        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080")); // Add your frontend URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
