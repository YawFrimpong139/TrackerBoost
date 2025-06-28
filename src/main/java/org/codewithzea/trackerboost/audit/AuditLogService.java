package org.codewithzea.trackerboost.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String actionType, String entityType, String entityId, String payloadJson) {
        log(actionType, entityType, entityId, "SYSTEM", payloadJson);
    }

    // Original log method (kept for potential future use)
    public void log(String actionType, String entityType, String entityId, String actorName, String payloadJson) {
        AuditLog log = AuditLog.builder()
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .actorName(actorName != null ? actorName : "SYSTEM")
                .payload(payloadJson)
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(log);
    }


    public List<AuditLog> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }

    public List<AuditLog> getLogsByActorName(String actorName) {
        return auditLogRepository.findByActorName(actorName);
    }

    public List<AuditLogDTO> getLogs(String entityType, String actorName) {
        List<AuditLog> logs;

        if (entityType != null && actorName != null) {
            logs = auditLogRepository.findByEntityTypeAndActorName(entityType, actorName);
        } else if (entityType != null) {
            logs = auditLogRepository.findByEntityType(entityType);
        } else if (actorName != null) {
            logs = auditLogRepository.findByActorName(actorName);
        } else {
            logs = auditLogRepository.findAll();
        }

        return logs.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AuditLogDTO toDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .timestamp(log.getTimestamp())
                .actorName(log.getActorName())
                .payload(log.getPayload())
                .build();
    }
}



