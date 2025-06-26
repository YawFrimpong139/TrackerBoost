package org.codewithzea.trackerboost;



import org.codewithzea.trackerboost.audit.SecurityAuditService;
import org.codewithzea.trackerboost.security.auth.AuthenticationEventListener;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationEventListenerTest {

    @Mock
    private SecurityAuditService securityAuditService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticationEventListener authenticationEventListener;

    @Test
    void handleSuccessfulAuthentication_ShouldLogSuccess() {
        // Arrange
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", "password");

        InteractiveAuthenticationSuccessEvent event =
                new InteractiveAuthenticationSuccessEvent(auth, this.getClass());

        // Act
        authenticationEventListener.handleSuccessfulAuthentication(event);

        // Assert
        verify(securityAuditService).logLoginSuccess("user@example.com", "192.168.1.1");
    }

    @Test
    void handleFailedAuthentication_ShouldLogFailure() {
        // Arrange
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // Create a proper AuthenticationException
        AuthenticationException authException =
                new BadCredentialsException("Invalid credentials");

        // Create authentication with test user
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@example.com", "password");

        // Create failure event with the proper exception type
        AbstractAuthenticationFailureEvent event =
                new AuthenticationFailureBadCredentialsEvent(auth, authException);

        // Act
        authenticationEventListener.handleFailedAuthentication(event);

        // Assert
        verify(securityAuditService).logLoginFailure(
                "user@example.com",
                "192.168.1.1",
                "Invalid credentials");
    }
}

