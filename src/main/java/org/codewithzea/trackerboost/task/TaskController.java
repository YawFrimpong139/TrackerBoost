package org.codewithzea.trackerboost.task;







import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskSecurity taskSecurity;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Fetching all tasks - Page: {}, Size: {}, Sort: {}, Direction: {}",
                page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TaskDTO> tasks = taskService.getAllTasks(pageable);
        log.debug("Retrieved {} tasks", tasks.getNumberOfElements());
        return ResponseEntity.ok(tasks);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        log.info("Fetching task with ID: {}", id);
        TaskDTO task = taskService.getTaskById(id);
        log.debug("Retrieved task: {}", task);
        return ResponseEntity.ok(task);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProjectId(@PathVariable Long projectId) {
        log.info("Fetching tasks for project ID: {}", projectId);
        List<TaskDTO> tasks = taskService.getTasksByProjectId(projectId);
        log.debug("Retrieved {} tasks for project", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    @GetMapping("/developer/{developerId}")
    public ResponseEntity<List<TaskDTO>> getTasksByDeveloperId(@PathVariable Long developerId) {
        log.info("Fetching tasks assigned to developer ID: {}", developerId);
        List<TaskDTO> tasks = taskService.getTasksByDeveloperId(developerId);
        log.debug("Retrieved {} tasks for developer", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDTO>> getOverdueTasks() {
        log.info("Fetching overdue tasks");
        List<TaskDTO> tasks = taskService.getOverdueTasks();
        log.debug("Found {} overdue tasks", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO dto) throws Exception {
        log.info("Creating new task: {}", dto);
        TaskDTO createdTask = taskService.createTask(dto);
        log.info("Created task with ID: {}", createdTask.getId());
        return ResponseEntity.ok(createdTask);
    }

    @PreAuthorize("@accessChecker.isTaskOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO dto) throws Exception {
        log.info("Updating task with ID: {}, Data: {}", id, dto);
        TaskDTO updatedTask = taskService.updateTask(id, dto);
        log.info("Updated task with ID: {}", id);
        return ResponseEntity.ok(updatedTask);
    }

    @PreAuthorize("@taskSecurity.isTaskOwner(#id, authentication) or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Principal principal) {
        log.info("User '{}' attempting to delete task ID: {}", principal.getName(), id);
        if (taskSecurity.isTaskOwner(id, SecurityContextHolder.getContext().getAuthentication())) {
            log.debug("User is task owner - proceeding with deletion");
        } else {
            log.debug("User is ADMIN - proceeding with deletion");
        }
        taskService.deleteTask(id, principal.getName());
        log.info("Deleted task with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/counts/status")
    public ResponseEntity<Map<String, Long>> getTaskCountsByStatus() {
        log.info("Fetching task counts by status");
        Map<String, Long> counts = taskService.getTaskCountsByStatus();
        log.debug("Task status counts: {}", counts);
        return ResponseEntity.ok(counts);
    }
}
