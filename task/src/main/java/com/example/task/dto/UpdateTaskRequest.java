package com.example.task.dto;

import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTaskRequest {
    @Size(min = 3, max = 200, message = "Title must be 3-200 characters")
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    private TaskStatus status;
    private TaskPriority priority;

    @NotNull(message = "Assignee ID cannot be null")
    private Long assigneeId;
}
