package org.codewithzea.trackerboost.project;



import org.codewithzea.trackerboost.audit.AuditLogService;
import org.codewithzea.trackerboost.user.Role;

import org.codewithzea.trackerboost.exception.ResourceNotFoundException;

import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.UserRepository;
import org.codewithzea.trackerboost.util.MapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;




@Service
@RequiredArgsConstructor
public class ProjectService {

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

    public Page<ProjectDTO> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(MapperUtil::toProjectDTO);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectDTO createProject(ProjectDTO dto) throws Exception {

        if (dto.getManagerId() == null) {
            throw new IllegalArgumentException("Manager ID must be provided");
        }

        // 2. Fetch manager (throws exception if not found)
        UserEntity manager = userRepository.findById(dto.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + dto.getManagerId()));

        // 3. Verify manager role
        if (!manager.getRole().equals(Role.ROLE_MANAGER)) {
            throw new IllegalArgumentException("User with ID " + dto.getManagerId() + " is not a manager");
        }

        // 4. Build and save project
        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .status(ProjectStatus.valueOf(dto.getStatus()))
                .manager(manager) // Set the validated manager
                .build();

        Project saved = projectRepository.save(project);
        return MapperUtil.toProjectDTO(saved);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public ProjectDTO updateProject(Long id, ProjectDTO dto) throws Exception {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));

        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        project.setStatus(ProjectStatus.valueOf(dto.getStatus()));

        Project updated = projectRepository.save(project);
        String payload = objectMapper.writeValueAsString(updated);
        auditLogService.log("UPDATE", "Project", updated.getId().toString(), payload);
        return MapperUtil.toProjectDTO(updated);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
        projectRepository.delete(project);
        auditLogService.log("DELETE", "Project", id.toString(), "");
    }

    public List<ProjectDTO> findProjectsWithoutTasks() {
        return projectRepository.findProjectsWithoutTasks().stream()
                .map(MapperUtil::toProjectDTO)
                .collect(Collectors.toList());
    }
}




