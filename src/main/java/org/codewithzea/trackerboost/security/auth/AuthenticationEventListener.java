package org.codewithzea.trackerboost.security.auth;


import org.codewithzea.trackerboost.audit.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {
    private final SecurityAuditService securityAuditService;

    @EventListener
    public void handleSuccessfulAuthentication(InteractiveAuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication.getName();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        securityAuditService.logLoginSuccess(username, request.getRemoteAddr());
    }

    @EventListener
    public void handleFailedAuthentication(AbstractAuthenticationFailureEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication != null ?
                (String) authentication.getPrincipal() : "unknown";

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        securityAuditService.logLoginFailure(
                username,
                request.getRemoteAddr(),
                event.getException().getMessage()
        );
    }
}
