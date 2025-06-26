package org.codewithzea.trackerboost;


import org.codewithzea.trackerboost.audit.AuditLogService;
import org.codewithzea.trackerboost.developer.Developer;
import org.codewithzea.trackerboost.developer.DeveloperRepository;
import org.codewithzea.trackerboost.project.Project;
import org.codewithzea.trackerboost.project.ProjectRepository;
import org.codewithzea.trackerboost.task.Task;
import org.codewithzea.trackerboost.task.TaskDTO;
import org.codewithzea.trackerboost.task.TaskRepository;
import org.codewithzea.trackerboost.task.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceAuditTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DeveloperRepository developerRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TaskService taskService;

    @Captor
    private ArgumentCaptor<String> actionTypeCaptor;

    @Captor
    private ArgumentCaptor<String> entityTypeCaptor;

    @Captor
    private ArgumentCaptor<String> entityIdCaptor;

    @Captor
    private ArgumentCaptor<String> payloadCaptor;

    private TaskDTO testTaskDTO;
    private Task testTask;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testTaskDTO = new TaskDTO();
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setStatus("PENDING");
        testTaskDTO.setProjectId(1L);
        testTaskDTO.setAssignedDeveloperIds(Set.of(1L));

        testProject = new Project();
        testProject.setId(1L);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setProject(testProject);
    }

    @Test
    void createTask_ShouldLogAuditEntry() throws Exception {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(developerRepository.findById(1L)).thenReturn(Optional.of(new Developer()));
        when(taskRepository.save(any())).thenReturn(testTask);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        taskService.createTask(testTaskDTO);

        // Assert
        verify(auditLogService).log(
                actionTypeCaptor.capture(),
                entityTypeCaptor.capture(),
                entityIdCaptor.capture(),
                payloadCaptor.capture());

        assertEquals("CREATE", actionTypeCaptor.getValue());
        assertEquals("Task", entityTypeCaptor.getValue());
        assertEquals("1", entityIdCaptor.getValue());
        assertEquals("{}", payloadCaptor.getValue());
    }

    @Test
    void updateTask_ShouldLogAuditEntry() throws Exception {
        // Arrange
        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Task");
        updatedTask.setProject(testProject);

        lenient().when(developerRepository.findById(1L)).thenReturn(Optional.of(new Developer()));
        lenient().when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask); // Return the updated task
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // Act
        taskService.updateTask(1L, testTaskDTO);

        // Assert
        verify(auditLogService).log(
                eq("UPDATE"),
                eq("Task"),
                eq("1"), // Verify it uses the ID from the updated task
                anyString());
    }

    @Test
    void deleteTask_ShouldLogAuditEntry() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        taskService.deleteTask(1L, "admin@example.com");

        // Assert
        verify(auditLogService).log(
                eq("DELETE"),
                eq("Task"),
                eq("1"),
                eq("admin@example.com"),
                anyString());
    }

    @Test
    void createTask_WithJsonProcessingError_ShouldStillComplete() throws JsonProcessingException {
        // Arrange
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(testProject));
        when(developerRepository.findById(anyLong())).thenReturn(Optional.of(new Developer()));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Force JSON processing to fail
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Test error") {});

        // Act
        TaskDTO result = taskService.createTask(testTaskDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testTaskDTO.getTitle(), result.getTitle());

        // Verify task was saved
        verify(taskRepository).save(any(Task.class));

        // Verify audit logging was called with fallback message
        verify(auditLogService).log(
                eq("CREATE"),
                eq("Task"),
                eq(testTask.getId().toString()),
                contains("Serialization error"));
    }

    private TaskDTO createValidTaskDTO() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Test Task");
        dto.setDescription("Test Description");
        dto.setStatus("PENDING");
        dto.setProjectId(1L);
        dto.setAssignedDeveloperIds(Set.of(1L));
        return dto;
    }

    private Project createTestProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        return project;
    }

    private Task createTestTask() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        return task;
    }
}