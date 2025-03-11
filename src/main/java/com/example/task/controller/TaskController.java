package com.example.task.controller;

import com.example.task.dto.*;
import com.example.task.entity.User;
import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.UserRepository;
import com.example.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);


    private final TaskService taskService;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Operation(summary = "Получить задачи по автору", description = "Возвращает список задач по ID автора")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
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
        List<TaskDTO> tasks = taskService.getTasksByAssignee(assigneeId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Создать новую задачу")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ) {
        User author = (User) authentication.getPrincipal();
        TaskDTO taskDTO = taskService.createTask(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority(),
                author,
                request.getAssigneeId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskDTO);
    }

    @Operation(summary = "Обновить задачу")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request) {
        TaskDTO taskDTO = taskService.updateTask(
                taskId,
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority(),
                request.getAssigneeId()
        );
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(summary = "Добавить комментарий к задаче")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long taskId,
            @RequestBody AddCommentRequest request,
            Authentication authentication) {
        User author = (User) authentication.getPrincipal();
        CommentDTO commentDTO = taskService.addComment(taskId, request.getText(), author);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDTO);
    }
    @Operation(summary = "Изменить приоритет задачи (только для админов)")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<TaskDTO> updateTaskPriority(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
            @Valid @RequestBody UpdatePriorityRequest request
    ) {
        TaskDTO taskDTO = taskService.updateTaskPriority(taskId, request.getPriority());
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(summary = "Обновить статус задачи")
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        TaskDTO taskDTO = taskService.updateTaskStatus(taskId, request.getStatus(), currentUser);
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(summary = "Удалить задачу")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Назначить задачу исполнителю")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/{taskId}/assign/{assigneeId}")
    public ResponseEntity<TaskDTO> assignTask(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
            @Parameter(description = "ID исполнителя") @PathVariable Long assigneeId) {
        TaskDTO taskDTO = taskService.assignTask(taskId, assigneeId);
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(summary = "Получить задачи с фильтрацией")
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TaskDTO> tasks = taskService.getTasks(status, priority, authorId, assigneeId, page, size);
        return ResponseEntity.ok(tasks);
    }
    @Operation(
            summary = "Получить все задачи (только для админов)",
            description = "Возвращает полный список задач с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный запрос"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы (1-100)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<TaskDTO> tasks = taskService.getAllTasks(page, size);
        return ResponseEntity.ok(tasks);
    }
}