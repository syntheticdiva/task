package com.example.task.dto;

import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    @NotNull(message = "Assignee ID is mandatory")
    private Long assigneeId;

}