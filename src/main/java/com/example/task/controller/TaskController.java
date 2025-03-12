package com.example.task.controller;

import com.example.task.dto.*;
import com.example.task.entity.User;
import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.exception.UserNotFoundException;
import com.example.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "API для управления задачами")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    @Operation(
            summary = "Получить задачи по автору",
            description = "Возвращает список задач, созданных указанным автором.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Автор не найден")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<TaskDTO>> getTasksByAuthor(
            @Parameter(description = "ID автора задачи", example = "1") @PathVariable Long authorId) {
        List<TaskDTO> tasks = taskService.getTasksByAuthor(authorId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Получить задачи по исполнителю",
            description = "Возвращает список задач, назначенных указанному исполнителю.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Исполнитель не найден")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(
            @PathVariable Long assigneeId) {
        List<TaskDTO> tasks = taskService.getTasksByAssignee(assigneeId);
        return ResponseEntity.ok(tasks);
    }
    @Operation(
            summary = "Создать новую задачу",
            description = "Создает новую задачу с указанными параметрами.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Задача успешно создана",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<TaskDTO> createTask(
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

    @Operation(
            summary = "Обновить задачу",
            description = "Обновляет данные задачи по её ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно обновлена",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @Parameter(description = "ID задачи", example = "1") @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
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

    @Operation(
            summary = "Добавить комментарий к задаче",
            description = "Добавляет комментарий к задаче по её ID.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Комментарий успешно добавлен",
                            content = @Content(schema = @Schema(implementation = CommentDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @Parameter(description = "ID задачи", example = "1") @PathVariable Long taskId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        User author = (User) authentication.getPrincipal();
        CommentDTO commentDTO = taskService.addComment(taskId, request.getText(), author);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDTO);
    }

    @Operation(
            summary = "Изменить приоритет задачи",
            description = "Изменяет приоритет задачи по её ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Приоритет задачи успешно обновлен",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<TaskDTO> updateTaskPriority(
            @Parameter(description = "ID задачи", example = "1") @PathVariable Long taskId,
            @Valid @RequestBody UpdatePriorityRequest request
    ) {
        TaskDTO taskDTO = taskService.updateTaskPriority(taskId, request.getPriority());
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(
            summary = "Обновить статус задачи",
            description = "Обновляет статус задачи по её ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус задачи успешно обновлен",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @Parameter(description = "ID задачи", example = "1") @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        TaskDTO taskDTO = taskService.updateTaskStatus(taskId, request.getStatus(), currentUser);
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(
            summary = "Удалить задачу",
            description = "Удаляет задачу по её ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID задачи", example = "1") @PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Назначить задачу исполнителю",
            description = "Назначает задачу указанному исполнителю.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно назначена",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Задача или исполнитель не найдены",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/{taskId}/assign/{assigneeId}")
    public ResponseEntity<TaskDTO> assignTask(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable @Min(1) Long taskId,

            @Parameter(description = "ID исполнителя", example = "2")
            @PathVariable @Min(1) Long assigneeId) {

        logger.info("Assigning task ID {} to assignee ID {}", taskId, assigneeId);

        try {
            TaskDTO taskDTO = taskService.assignTask(taskId, assigneeId);
            return ResponseEntity.ok(taskDTO);
        } catch (TaskNotFoundException ex) {
            logger.error("Task assignment failed: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (UserNotFoundException ex) {
            logger.error("Assignee not found: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @Operation(
            summary = "Получить задачи с фильтрацией",
            description = "Возвращает список задач с возможностью фильтрации по статусу, приоритету, автору и исполнителю.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @Parameter(description = "Статус задачи", example = "IN_PROGRESS") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Приоритет задачи", example = "HIGH") @RequestParam(required = false) TaskPriority priority,
            @Parameter(description = "ID автора задачи", example = "1") @RequestParam(required = false) Long authorId,
            @Parameter(description = "ID исполнителя задачи", example = "2") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (1-100)", example = "10") @RequestParam(defaultValue = "10") int size) {
        Page<TaskDTO> tasks = taskService.getTasks(status, priority, authorId, assigneeId, page, size);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Получить все задачи (только для админов)",
            description = "Возвращает полный список задач с пагинацией.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены",
                            content = @Content(schema = @Schema(implementation = TaskDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (1-100)", example = "20") @RequestParam(defaultValue = "20") int size) {
        Page<TaskDTO> tasks = taskService.getAllTasks(page, size);
        return ResponseEntity.ok(tasks);
    }
}