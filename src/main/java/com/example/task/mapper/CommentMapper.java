package com.example.task.mapper;

import com.example.task.dto.CommentDTO;
import com.example.task.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "author.id", target = "authorId")
    CommentDTO toCommentDTO(Comment comment);

    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    Comment toComment(CommentDTO commentDTO);
}