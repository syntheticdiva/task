package com.example.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {
    private Long id;

    @NotBlank(message = "Text is required")
    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    private String text;

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Author ID is required")
    private Long authorId;
}
