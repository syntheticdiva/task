package com.example.task.dto;

import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import lombok.Data;

@Data
public class TaskFilterRequest {
    private TaskStatus status;
    private TaskPriority priority;
    private Long authorId;
    private Long assigneeId;
    private int page = 0;
    private int size = 10;
}
