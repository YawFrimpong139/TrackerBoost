package org.codewithzea.trackerboost.audit;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SecurityAuditService {
    private final AuditLogRepository auditLogRepository;

    public void logLoginSuccess(String username, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .actionType("LOGIN_SUCCESS")
                .entityType("Authentication")
                .entityId(username)
                .timestamp(Instant.now())
                .actorName(username)
                .ipAddress(ipAddress)
                .status("SUCCESS")
                .payload("User logged in successfully")
                .build();

        auditLogRepository.save(log);
    }

    public void logLoginFailure(String username, String ipAddress, String errorMessage) {
        AuditLog log = AuditLog.builder()
                .actionType("LOGIN_FAILURE")
                .entityType("Authentication")
                .entityId(username)
                .timestamp(Instant.now())
                .actorName(username)
                .ipAddress(ipAddress)
                .status("FAILURE")
                .payload(errorMessage)
                .build();

        auditLogRepository.save(log);
    }

    public void logUnauthorizedAccess(String action, String ipAddress, String errorMessage) {
        AuditLog log = AuditLog.builder()
                .actionType("ACCESS_DENIED")
                .entityType("Authorization")
                .timestamp(Instant.now())
                .ipAddress(ipAddress)
                .status("FAILURE")
                .payload(errorMessage)
                .build();

        auditLogRepository.save(log);
    }
}
