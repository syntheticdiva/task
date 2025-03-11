package com.example.task.mapper;

import com.example.task.dto.TaskDTO;
import com.example.task.entity.Task;
import com.example.task.entity.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public abstract class TaskMapper {

    @Mapping(source = "author", target = "authorId", qualifiedByName = "mapAuthorId")
    @Mapping(source = "assignee", target = "assigneeId", qualifiedByName = "mapAssigneeId")
    public abstract TaskDTO toTaskDTO(Task task);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    public abstract Task toTask(TaskDTO taskDTO);

    @Named("mapAuthorId")
    public Long mapAuthorId(User author) {
        return author != null ? author.getId() : null;
    }

    @Named("mapAssigneeId")
    public Long mapAssigneeId(User assignee) {
        return assignee != null ? assignee.getId() : null;
    }

}
