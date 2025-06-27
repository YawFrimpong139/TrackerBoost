package org.codewithzea.trackerboost.util;



import org.codewithzea.trackerboost.project.*;
import org.codewithzea.trackerboost.task.*;
import org.codewithzea.trackerboost.developer.*;
import org.codewithzea.trackerboost.user.UserEntity;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MapperUtil {

    // Project
    public static ProjectDTO toProjectDTO(Project project) {
        if (project == null) return null;
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .deadline(project.getDeadline())
                .status(String.valueOf(project.getStatus()))
                .managerId(project.getManager() != null ? project.getManager().getId() : null)
                .build();
    }

    public static Project toProject(ProjectDTO dto) {
        if (dto == null) return null;
        return Project.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .status(ProjectStatus.valueOf(dto.getStatus()))
                .build();
    }

    // Developer
    public static DeveloperDTO toDeveloperDTO(Developer dev) {
        if (dev == null) return null;
        return DeveloperDTO.builder()
                .id(dev.getId())
                .name(dev.getName())
                .email(dev.getEmail())
                .skills(dev.getSkills())
                .build();
    }

    public static Developer toDeveloper(DeveloperDTO dto, UserEntity user) {
        if (dto == null) return null;
        return Developer.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .skills(dto.getSkills())
                .user(user)
                .build();
    }

    // Task
    public static TaskDTO toTaskDTO(Task task) {
        if (task == null) return null;

        Set<Long> developerIds = task.getAssignedDevelopers() != null
                ? task.getAssignedDevelopers().stream()
                .map(Developer::getId)
                .collect(Collectors.toSet())
                : new HashSet<>();

        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .assignedDeveloperIds(developerIds)
                .build();
    }

}



