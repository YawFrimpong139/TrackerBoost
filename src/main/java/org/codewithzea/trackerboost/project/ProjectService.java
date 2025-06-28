package org.codewithzea.trackerboost.project;



import org.codewithzea.trackerboost.audit.AuditLogService;
import org.codewithzea.trackerboost.optimize.ProjectListDTO;
import org.codewithzea.trackerboost.user.Role;
import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.UserRepository;
import org.codewithzea.trackerboost.exception.ResourceNotFoundException;
import org.codewithzea.trackerboost.util.MapperUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "projects", key = "#id")
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
        return MapperUtil.toProjectDTO(project);
    }

    @Cacheable(value = "allProjects", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<ProjectDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(MapperUtil::toProjectDTO);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "allProjects", allEntries = true),
            @CacheEvict(value = "projectSummaries", allEntries = true),
            @CacheEvict(value = "projectsWithoutTasks", allEntries = true)
    })
    public ProjectDTO createProject(ProjectDTO dto) throws Exception {
        if (dto.managerId() == null) {
            throw new IllegalArgumentException("Manager ID must be provided");
        }

        UserEntity manager = userRepository.findById(dto.managerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + dto.managerId()));

        if (!manager.getRole().equals(Role.ROLE_MANAGER)) {
            throw new IllegalArgumentException("User with ID " + dto.managerId() + " is not a manager");
        }

        Project project = Project.builder()
                .name(dto.name())
                .description(dto.description())
                .deadline(dto.deadline())
                .status(ProjectStatus.valueOf(dto.status()))
                .manager(manager)
                .tasks(new ArrayList<>(10))
                .build();

        Project saved = projectRepository.save(project);

        try {
            String payload = objectMapper.writeValueAsString(saved);
            auditLogService.log("CREATE", "Project", saved.getId().toString(), payload);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize project for audit log", e);
            auditLogService.log("CREATE", "Project", saved.getId().toString(),
                    "Serialization error: " + e.getMessage());
        }

        return MapperUtil.toProjectDTO(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "allProjects", allEntries = true),
            @CacheEvict(value = "projectSummaries", allEntries = true),
            @CacheEvict(value = "projectsWithoutTasks", allEntries = true)
    })
    public ProjectDTO updateProject(Long id, ProjectDTO dto) throws Exception {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));

        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setDeadline(dto.deadline());
        project.setStatus(ProjectStatus.valueOf(dto.status()));

        Project updated = projectRepository.save(project);

        try {
            String payload = objectMapper.writeValueAsString(updated);
            auditLogService.log("UPDATE", "Project", updated.getId().toString(), payload);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize project for audit log", e);
            auditLogService.log("UPDATE", "Project", updated.getId().toString(),
                    "Serialization error: " + e.getMessage());
        }

        return MapperUtil.toProjectDTO(updated);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#id"),
            @CacheEvict(value = "allProjects", allEntries = true),
            @CacheEvict(value = "projectSummaries", allEntries = true),
            @CacheEvict(value = "projectsWithoutTasks", allEntries = true),
            @CacheEvict(value = "projectTasks", allEntries = true)
    })
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));

        projectRepository.delete(project);
        auditLogService.log("DELETE", "Project", id.toString(), "");
    }

    @Cacheable(value = "projectSummaries", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<ProjectListDTO> getAllProjectSummaries(Pageable pageable) {
        return projectRepository.findAllSummaries(pageable);
    }

    @Cacheable(value = "projectsWithoutTasks")
    public List<ProjectDTO> findProjectsWithoutTasks() {
        return projectRepository.findProjectsWithoutTasks().stream()
                .map(MapperUtil::toProjectDTO)
                .collect(Collectors.toCollection(() -> new ArrayList<>(50)));
    }
}


