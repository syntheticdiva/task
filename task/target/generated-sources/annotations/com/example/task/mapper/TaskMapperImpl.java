package com.example.task.mapper;

import com.example.task.dto.CommentDTO;
import com.example.task.dto.TaskDTO;
import com.example.task.entity.Comment;
import com.example.task.entity.Task;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-03-12T14:23:12+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22 (Oracle Corporation)"
)
@Component
public class TaskMapperImpl extends TaskMapper {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public TaskDTO toTaskDTO(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskDTO taskDTO = new TaskDTO();

        taskDTO.setAuthorId( mapAuthorId( task.getAuthor() ) );
        taskDTO.setAssigneeId( mapAssigneeId( task.getAssignee() ) );
        taskDTO.setId( task.getId() );
        taskDTO.setTitle( task.getTitle() );
        taskDTO.setDescription( task.getDescription() );
        taskDTO.setStatus( task.getStatus() );
        taskDTO.setPriority( task.getPriority() );
        taskDTO.setComments( commentListToCommentDTOList( task.getComments() ) );

        return taskDTO;
    }

    @Override
    public Task toTask(TaskDTO taskDTO) {
        if ( taskDTO == null ) {
            return null;
        }

        Task task = new Task();

        task.setId( taskDTO.getId() );
        task.setTitle( taskDTO.getTitle() );
        task.setDescription( taskDTO.getDescription() );
        task.setStatus( taskDTO.getStatus() );
        task.setPriority( taskDTO.getPriority() );
        task.setComments( commentDTOListToCommentList( taskDTO.getComments() ) );

        return task;
    }

    protected List<CommentDTO> commentListToCommentDTOList(List<Comment> list) {
        if ( list == null ) {
            return null;
        }

        List<CommentDTO> list1 = new ArrayList<CommentDTO>( list.size() );
        for ( Comment comment : list ) {
            list1.add( commentMapper.toCommentDTO( comment ) );
        }

        return list1;
    }

    protected List<Comment> commentDTOListToCommentList(List<CommentDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<Comment> list1 = new ArrayList<Comment>( list.size() );
        for ( CommentDTO commentDTO : list ) {
            list1.add( commentMapper.toComment( commentDTO ) );
        }

        return list1;
    }
}
