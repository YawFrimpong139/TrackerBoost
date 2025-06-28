package org.codewithzea.trackerboost.project;




import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable Long id) {
        log.info("Fetching project with ID: {}", id);
        ProjectDTO project = projectService.getProjectById(id);
        log.debug("Retrieved project: {}", project);
        return ResponseEntity.ok(project);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    @GetMapping
    public ResponseEntity<Page<ProjectDTO>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Fetching all projects - Page: {}, Size: {}, Sort: {}, Direction: {}",
                page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProjectDTO> projects = projectService.getAllProjects(pageable);
        log.debug("Retrieved {} projects", projects.getNumberOfElements());
        return ResponseEntity.ok(projects);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO dto) throws Exception {
        log.info("Creating new project with manager ID: {}", dto.managerId());

        if (dto.managerId() == null) {
            throw new IllegalArgumentException("Manager ID is required");
        }

        ProjectDTO createdProject = projectService.createProject(dto);
        log.info("Created project with ID: {} for manager ID: {}",
                createdProject.id(), dto.managerId());

        return ResponseEntity.ok(createdProject);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectDTO dto) throws Exception {
        log.info("Updating project with ID: {}, Data: {}", id, dto);
        ProjectDTO updatedProject = projectService.updateProject(id, dto);
        log.info("Updated project with ID: {}", id);
        return ResponseEntity.ok(updatedProject);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("Deleting project with ID: {}", id);
        projectService.deleteProject(id);
        log.info("Deleted project with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER', 'CONTRACTOR')")
    @GetMapping("/without-tasks")
    public ResponseEntity<List<ProjectDTO>> getProjectsWithoutTasks() {
        log.info("Fetching projects without tasks");
        List<ProjectDTO> projects = projectService.findProjectsWithoutTasks();
        log.debug("Found {} projects without tasks", projects.size());
        return ResponseEntity.ok(projects);
    }
}


