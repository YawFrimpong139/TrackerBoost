package org.codewithzea.trackerboost.developer;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {

    // Find top 5 developers with most tasks assigned
    @Query("SELECT d FROM Developer d LEFT JOIN d.assignedTasks t GROUP BY d ORDER BY COUNT(t) DESC")
    List<Developer> findTop5ByTaskCount(Pageable pageable);

    boolean existsByEmail(String email);
}


