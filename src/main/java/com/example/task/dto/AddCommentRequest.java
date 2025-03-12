package com.example.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddCommentRequest {
    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    private String text;
}
