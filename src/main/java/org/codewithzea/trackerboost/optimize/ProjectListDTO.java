package org.codewithzea.trackerboost.optimize;

import org.codewithzea.trackerboost.project.ProjectStatus;

public record ProjectListDTO(
        Long id,
        String name,
        ProjectStatus status
) {}

