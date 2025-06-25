package org.codewithzea.trackerboost.task;


import org.codewithzea.trackerboost.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component("taskSecurity")
@RequiredArgsConstructor
public class TaskSecurity {

    private final TaskRepository taskRepository;

    public boolean isTaskOwner(Long taskId, Authentication authentication) {
        log.debug("Checking task ownership for task ID: {}", taskId);
        Task task = taskRepository.findById(taskId).orElseThrow(() -> {
            log.error("Task not found with ID: {}", taskId);
            return new RuntimeException("Task not found");
        });

        UserEntity currentUser = (UserEntity) authentication.getPrincipal();
        boolean isOwner = task.getAssignedDevelopers().stream()
                .anyMatch(developer -> developer.getId().equals(currentUser.getId()));

        log.debug("User {} {} task owner for task ID: {}",
                currentUser.getEmail(),
                isOwner ? "is" : "is not",
                taskId);

        return isOwner;
    }
}
