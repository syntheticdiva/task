package com.example.task.mapper;

import com.example.task.dto.CommentDTO;
import com.example.task.entity.Comment;
import com.example.task.entity.Task;
import com.example.task.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-03-12T14:23:13+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22 (Oracle Corporation)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentDTO toCommentDTO(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setTaskId( commentTaskId( comment ) );
        commentDTO.setAuthorId( commentAuthorId( comment ) );
        commentDTO.setId( comment.getId() );
        commentDTO.setText( comment.getText() );

        return commentDTO;
    }

    @Override
    public Comment toComment(CommentDTO commentDTO) {
        if ( commentDTO == null ) {
            return null;
        }

        Comment comment = new Comment();

        comment.setId( commentDTO.getId() );
        comment.setText( commentDTO.getText() );

        return comment;
    }

    private Long commentTaskId(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        Task task = comment.getTask();
        if ( task == null ) {
            return null;
        }
        Long id = task.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long commentAuthorId(Comment comment) {
        if ( comment == null ) {
            return null;
        }
        User author = comment.getAuthor();
        if ( author == null ) {
            return null;
        }
        Long id = author.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
