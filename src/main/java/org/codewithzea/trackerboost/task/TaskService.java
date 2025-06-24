package org.codewithzea.trackerboost.task;


import org.codewithzea.trackerboost.developer.Developer;
import org.codewithzea.trackerboost.developer.DeveloperRepository;
import org.codewithzea.trackerboost.audit.AuditLogService;
import org.codewithzea.trackerboost.project.Project;
import org.codewithzea.trackerboost.project.ProjectRepository;
import org.codewithzea.trackerboost.exception.ResourceNotFoundException;
import org.codewithzea.trackerboost.util.MapperUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public Page<TaskDTO> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(MapperUtil::toTaskDTO);
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
        return MapperUtil.toTaskDTO(task);
    }

    public List<TaskDTO> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(MapperUtil::toTaskDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByDeveloperId(Long developerId) {
        return taskRepository.findByDeveloperId(developerId).stream()
                .map(MapperUtil::toTaskDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getOverdueTasks() {
        return taskRepository.findOverdueTasks().stream()
                .map(MapperUtil::toTaskDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO createTask(TaskDTO dto) {
        try {
            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + dto.getProjectId()));

            Task task = Task.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .status(dto.getStatus())
                    .dueDate(dto.getDueDate())
                    .project(project)
                    .build();

            if (dto.getAssignedDeveloperIds() != null && !dto.getAssignedDeveloperIds().isEmpty()) {
                assignDevelopersToTask(dto, task);
            }

            Task savedTask = taskRepository.save(task);
            logTaskCreation(savedTask);

            return MapperUtil.toTaskDTO(savedTask);
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw business exceptions
        } catch (Exception e) {
            logger.error("Failed to create task", e);
            throw new RuntimeException("Failed to create task", e);
        }
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO dto) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));

            updateTaskProperties(dto, task);
            Task updatedTask = taskRepository.save(task);
            logTaskUpdate(updatedTask);

            return MapperUtil.toTaskDTO(updatedTask);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update task with id: " + id, e);
            throw new RuntimeException("Failed to update task", e);
        }
    }

    @Transactional
    public void deleteTask(Long id, String actorName) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));

            taskRepository.delete(task);
            auditLogService.log("DELETE", "Task", id.toString(), actorName, "");
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete task with id: " + id, e);
            throw new RuntimeException("Failed to delete task", e);
        }
    }

    public Map<String, Long> getTaskCountsByStatus() {
        try {
            List<Object[]> counts = taskRepository.countTasksGroupedByStatus();
            Map<String, Long> result = new HashMap<>();

            for (Object[] obj : counts) {
                String status = (String) obj[0];
                Long count = (Long) obj[1];
                result.put(status, count);
            }

            return result;
        } catch (Exception e) {
            logger.error("Failed to get task counts by status", e);
            throw new RuntimeException("Failed to get task statistics", e);
        }
    }

    private void assignDevelopersToTask(TaskDTO dto, Task task) {
        Set<Developer> developers = new HashSet<>();
        for (Long devId : dto.getAssignedDeveloperIds()) {
            Developer dev = developerRepository.findById(devId)
                    .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id " + devId));
            developers.add(dev);
        }
        task.setAssignedDevelopers(developers);
    }

    private void updateTaskProperties(TaskDTO dto, Task task) {
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());

        if (!task.getProject().getId().equals(dto.getProjectId())) {
            Project project = projectRepository.findById(dto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + dto.getProjectId()));
            task.setProject(project);
        }

        if (dto.getAssignedDeveloperIds() != null) {
            assignDevelopersToTask(dto, task);
        }
    }

    private void logTaskCreation(Task task) {
        try {
            String payload = objectMapper.writeValueAsString(task);
            auditLogService.log("CREATE", "Task", task.getId().toString(), payload);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize task for audit log", e);
            auditLogService.log("CREATE", "Task", task.getId().toString(),
                    "Serialization error: " + e.getMessage());
        }
    }

    private void logTaskUpdate(Task task) {
        try {
            String payload = objectMapper.writeValueAsString(task);
            auditLogService.log("UPDATE", "Task", task.getId().toString(), payload);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize task for audit log", e);
            auditLogService.log("UPDATE", "Task", task.getId().toString(),
                    "Serialization error: " + e.getMessage());
        }
    }
}
