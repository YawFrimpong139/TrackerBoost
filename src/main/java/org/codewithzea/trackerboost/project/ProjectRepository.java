package org.codewithzea.trackerboost.project;


import org.codewithzea.trackerboost.optimize.ProjectListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Fetch projects without any tasks
    @Query("SELECT p FROM Project p WHERE p.tasks IS EMPTY")
    List<Project> findProjectsWithoutTasks();

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Project p " +
            "WHERE p.id = :projectId AND p.manager.id = :userId")
    boolean existsByIdAndManagerId(@Param("projectId") Long projectId,
                                   @Param("userId") Long userId);

    @Query("SELECT new org.codewithzea.trackerboost.optimize.ProjectListDTO(p.id, p.name, p.status) FROM Project p")
    Page<ProjectListDTO> findAllSummaries(Pageable pageable);
}



