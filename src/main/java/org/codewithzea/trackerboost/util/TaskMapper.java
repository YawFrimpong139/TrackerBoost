package org.codewithzea.trackerboost.util;



import org.codewithzea.trackerboost.optimize.TaskSummaryDTO;
import org.codewithzea.trackerboost.task.Task;
import org.codewithzea.trackerboost.task.TaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "projectId", source = "project.id")
    TaskDTO toDto(Task task);

    TaskSummaryDTO toSummaryDto(Task task);
}
