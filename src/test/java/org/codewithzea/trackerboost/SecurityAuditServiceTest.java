package org.codewithzea.trackerboost;


import org.codewithzea.trackerboost.audit.AuditLog;
import org.codewithzea.trackerboost.audit.AuditLogRepository;
import org.codewithzea.trackerboost.audit.SecurityAuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityAuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private SecurityAuditService securityAuditService;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    private final String testUsername = "test@example.com";
    private final String testIpAddress = "192.168.1.1";
    private final String testErrorMessage = "Invalid credentials";

    @Test
    void logLoginSuccess_ShouldCreateCorrectAuditLog() {
        // Act
        securityAuditService.logLoginSuccess(testUsername, testIpAddress);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertAll(
                () -> assertEquals("LOGIN_SUCCESS", savedLog.getActionType()),
                () -> assertEquals("Authentication", savedLog.getEntityType()),
                () -> assertEquals(testUsername, savedLog.getEntityId()),
                () -> assertEquals(testUsername, savedLog.getActorName()),
                () -> assertEquals(testIpAddress, savedLog.getIpAddress()),
                () -> assertEquals("SUCCESS", savedLog.getStatus()),
                () -> assertEquals("User logged in successfully", savedLog.getPayload()),
                () -> assertNotNull(savedLog.getTimestamp())
        );
    }

    @Test
    void logLoginFailure_ShouldCreateCorrectAuditLog() {
        // Act
        securityAuditService.logLoginFailure(testUsername, testIpAddress, testErrorMessage);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertAll(
                () -> assertEquals("LOGIN_FAILURE", savedLog.getActionType()),
                () -> assertEquals("Authentication", savedLog.getEntityType()),
                () -> assertEquals(testUsername, savedLog.getEntityId()),
                () -> assertEquals(testUsername, savedLog.getActorName()),
                () -> assertEquals(testIpAddress, savedLog.getIpAddress()),
                () -> assertEquals("FAILURE", savedLog.getStatus()),
                () -> assertEquals(testErrorMessage, savedLog.getPayload()),
                () -> assertNotNull(savedLog.getTimestamp())
        );
    }

    @Test
    void logUnauthorizedAccess_ShouldCreateCorrectAuditLog() {
        // Arrange
        String action = "ACCESS_DENIED";

        // Act
        securityAuditService.logUnauthorizedAccess(action, testIpAddress, testErrorMessage);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertAll(
                () -> assertEquals("ACCESS_DENIED", savedLog.getActionType()),
                () -> assertEquals("Authorization", savedLog.getEntityType()),
                () -> assertEquals(testIpAddress, savedLog.getIpAddress()),
                () -> assertEquals("FAILURE", savedLog.getStatus()),
                () -> assertEquals(testErrorMessage, savedLog.getPayload()),
                () -> assertNotNull(savedLog.getTimestamp()),
                () -> assertNull(savedLog.getEntityId()),
                () -> assertNull(savedLog.getActorName())
        );
    }
}
