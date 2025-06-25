package org.codewithzea.trackerboost.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    private String actionType;   // CREATE, UPDATE, DELETE
    private String entityType;   // Project, Task, Developer
    private String entityId;
    private Instant timestamp;
    private String actorName;
    private String ipAddress;
    private String status;
    private String payload;      // JSON snapshot of the entity
}



