package com.example.task.controller;

import com.example.task.dto.*;
import com.example.task.entity.Comment;
import com.example.task.entity.Task;
import com.example.task.entity.User;
import com.example.task.enums.Role;
import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import com.example.task.repository.TaskRepository;
import com.example.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    @Autowired
    private final TaskService taskService;
    @Autowired
    private final TaskRepository taskRepository;

    public TaskController(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    @Operation(summary = "Получить задачи по автору", description = "Возвращает список задач по ID автора")
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<TaskDTO>> getTasksByAuthor(
            @Parameter(description = "ID автора задачи") @PathVariable Long authorId) {
        List<TaskDTO> tasks = taskService.getTasksByAuthor(authorId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Получить задачи по исполнителю", description = "Возвращает список задач по ID исполнителя")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(
            @Parameter(description = "ID исполнителя задачи") @PathVariable Long assigneeId) {
        return ResponseEntity.ok(taskService.getTasksByAssignee(assigneeId));
    }

    @Operation(summary = "Создать новую задачу")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request, Authentication authentication) {
        // Получаем текущего пользователя (автора задачи)
        User author = (User) authentication.getPrincipal();

        // Создаем задачу
        Task task = taskService.createTask(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority(),
                author,
                request.getAssigneeId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request
    ) {
        Task task = taskService.updateTask(
                taskId,
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority(),
                request.getAssigneeId()
        );
        return ResponseEntity.ok(task);
    }
    @PostMapping("/{taskId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long taskId,
            @RequestBody AddCommentRequest request,
            Authentication authentication
    ) {
        User author = (User) authentication.getPrincipal();
        CommentDTO commentDTO = taskService.addComment(taskId, request.getText(), author);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDTO);
    }
    @Operation(
            summary = "Update task status",
            description = "Обновление статуса задачи. Доступно администраторам и исполнителю задачи",
            security = @SecurityRequirement(name = "Bearer Authentication", scopes = {"ADMIN", "USER"}),
            parameters = {
                    @Parameter(
                            name = "taskId",
                            in = ParameterIn.PATH,
                            description = "ID обновляемой задачи",
                            example = "1",
                            required = true
                    )
            }
    )
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        TaskDTO taskDTO = taskService.updateTaskStatus(taskId, request.getStatus(), currentUser);
        return ResponseEntity.ok(taskDTO);
    }
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Назначить задачу исполнителю", description = "Назначает задачу исполнителю по ID задачи и ID исполнителя")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/{taskId}/assign/{assigneeId}")
    public ResponseEntity<TaskDTO> assignTask(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
            @Parameter(description = "ID исполнителя") @PathVariable Long assigneeId) {
        TaskDTO taskDTO = taskService.assignTask(taskId, assigneeId);
        return ResponseEntity.ok(taskDTO);
    }
    @GetMapping
    @Operation(
            summary = "Get tasks",
            description = "Returns filtered and paginated tasks. Requires at least one filter parameter",
            parameters = {
                    @Parameter(name = "status", description = "Task status filter"),
                    @Parameter(name = "priority", description = "Task priority filter"),
                    @Parameter(name = "authorId", description = "Author ID filter"),
                    @Parameter(name = "assigneeId", description = "Assignee ID filter"),
                    @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size (1-100)", example = "20")
            },
            responses = {
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "404", description = "No tasks found"),
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks")
            }
    )
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TaskDTO> tasks = taskService.getTasks(status, priority, authorId, assigneeId, page, size);
        return ResponseEntity.ok(tasks);
    }
}