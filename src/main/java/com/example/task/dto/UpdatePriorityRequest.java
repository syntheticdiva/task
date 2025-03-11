package com.example.task.dto;

import com.example.task.enums.TaskPriority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePriorityRequest {
    @NotNull(message = "Priority is required")
    private TaskPriority priority;
}