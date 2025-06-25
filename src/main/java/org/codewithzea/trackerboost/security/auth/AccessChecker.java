package org.codewithzea.trackerboost.security.auth;


import org.codewithzea.trackerboost.project.ProjectRepository;
import org.codewithzea.trackerboost.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("accessChecker")
@Slf4j
@RequiredArgsConstructor
public class AccessChecker {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public boolean isTaskOwner(Long taskId, Long userId) {
        log.debug("Checking task ownership for task {} and user {}", taskId, userId);
        return taskRepository.existsByIdAndAssignedDevelopers_Id(taskId, userId);
    }

    public boolean isProjectManager(Long projectId, Long userId) {
        log.debug("Checking project management for project {} and user {}", projectId, userId);
        return projectRepository.existsByIdAndManagerId(projectId, userId);
    }
}

