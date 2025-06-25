package org.codewithzea.trackerboost;



import org.codewithzea.trackerboost.audit.AuditLog;
import org.codewithzea.trackerboost.audit.AuditLogDTO;
import org.codewithzea.trackerboost.audit.AuditLogRepository;
import org.codewithzea.trackerboost.audit.AuditLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    private final String testActionType = "CREATE";
    private final String testEntityType = "Task";
    private final String testEntityId = "123";
    private final String testActorName = "user@example.com";
    private final String testPayload = "{\"id\":123,\"title\":\"Test Task\"}";

    @Test
    void log_WithActorName_ShouldCreateCorrectAuditLog() throws JsonProcessingException {
        // Act
        auditLogService.log(testActionType, testEntityType, testEntityId, testActorName, testPayload);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertAll(
                () -> assertEquals(testActionType, savedLog.getActionType()),
                () -> assertEquals(testEntityType, savedLog.getEntityType()),
                () -> assertEquals(testEntityId, savedLog.getEntityId()),
                () -> assertEquals(testActorName, savedLog.getActorName()),
                () -> assertEquals(testPayload, savedLog.getPayload()),
                () -> assertNotNull(savedLog.getTimestamp())
        );
    }

    @Test
    void log_WithoutActorName_ShouldUseSystemAsDefault() throws JsonProcessingException {
        // Act
        auditLogService.log(testActionType, testEntityType, testEntityId, testPayload);

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());
        AuditLog savedLog = auditLogCaptor.getValue();

        assertEquals("SYSTEM", savedLog.getActorName());
    }

    @Test
    void log_WithJsonProcessingException_ShouldStillSaveLog() throws JsonProcessingException {
        // Arrange
        lenient().when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Error") {});

        // Act
        assertDoesNotThrow(() ->
                auditLogService.log(testActionType, testEntityType, testEntityId, testPayload));

        // Assert
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertNotNull(savedLog);
        assertEquals(testActionType, savedLog.getActionType());
        assertEquals(testEntityType, savedLog.getEntityType());
        assertEquals(testEntityId, savedLog.getEntityId());
        assertNotNull(savedLog.getTimestamp());
    }
    @Test
    void getLogs_WithEntityTypeAndActorName_ShouldCallCorrectRepositoryMethod() {
        // Arrange
        when(auditLogRepository.findByEntityTypeAndActorName(testEntityType, testActorName))
                .thenReturn(List.of(new AuditLog()));

        // Act
        List<AuditLogDTO> result = auditLogService.getLogs(testEntityType, testActorName);

        // Assert
        assertEquals(1, result.size());
        verify(auditLogRepository).findByEntityTypeAndActorName(testEntityType, testActorName);
    }

    @Test
    void getLogs_WithEntityTypeOnly_ShouldCallCorrectRepositoryMethod() {
        // Arrange
        when(auditLogRepository.findByEntityType(testEntityType))
                .thenReturn(List.of(new AuditLog(), new AuditLog()));

        // Act
        List<AuditLogDTO> result = auditLogService.getLogs(testEntityType, null);

        // Assert
        assertEquals(2, result.size());
        verify(auditLogRepository).findByEntityType(testEntityType);
    }

    @Test
    void getLogs_WithNoFilters_ShouldCallFindAll() {
        // Arrange
        when(auditLogRepository.findAll()).thenReturn(List.of(new AuditLog()));

        // Act
        List<AuditLogDTO> result = auditLogService.getLogs(null, null);

        // Assert
        assertEquals(1, result.size());
        verify(auditLogRepository).findAll();
    }
}