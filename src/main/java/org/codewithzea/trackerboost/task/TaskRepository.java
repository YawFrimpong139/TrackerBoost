package org.codewithzea.trackerboost.task;






import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find all tasks by project id
    List<Task> findByProjectId(Long projectId);

    // Find all tasks assigned to a developer
    @Query("SELECT t FROM Task t JOIN t.assignedDevelopers d WHERE d.id = :developerId")
    List<Task> findByDeveloperId(@Param("developerId") Long developerId);

    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status <> 'DONE'")
    List<Task> findOverdueTasks();

    // Task counts grouped by status
    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t GROUP BY t.status")
    List<Object[]> countTasksGroupedByStatus();

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Task t JOIN t.assignedDevelopers d " +
            "WHERE t.id = :taskId AND d.id = :userId")
    boolean existsByIdAndAssignedDevelopers_Id(@Param("taskId") Long taskId,
                                               @Param("userId") Long userId);
}
