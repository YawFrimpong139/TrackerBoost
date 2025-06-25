package org.codewithzea.trackerboost.audit;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByEntityType(String entityType);

    List<AuditLog> findByActorName(String actorName);

    List<AuditLog> findByEntityTypeAndActorName(String entityType, String actorName);
}



