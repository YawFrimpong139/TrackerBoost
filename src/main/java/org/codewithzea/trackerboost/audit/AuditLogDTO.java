package org.codewithzea.trackerboost.audit;


import lombok.Builder;
import java.time.Instant;

@Builder
public record AuditLogDTO(
        String id,
        String actionType,
        String entityType,
        String entityId,
        Instant timestamp,
        String actorName,
        String payload
) {
    public static class AuditLogDTOBuilder {
        public AuditLogDTO build() {
            if (this.timestamp == null) {
                this.timestamp = Instant.now();
            }
            return new AuditLogDTO(id, actionType, entityType, entityId, timestamp, actorName, payload);
        }
    }
}


