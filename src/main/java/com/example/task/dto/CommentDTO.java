package com.example.task.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long id;
    private String text;
    private Long taskId;
    private Long authorId;
}
