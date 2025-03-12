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
@RequestMapping(TaskController.API_TASKS_PATH)
@Tag(name = "Task Management", description = "API для управления задачами")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class TaskController {
    public static final String API_TASKS_PATH = "/api/tasks";
    private static final String AUTHOR_PATH = "/author/{authorId}";
    private static final String ASSIGNEE_PATH = "/assignee/{assigneeId}";
    private static final String CREATE_PATH = "/create";
    private static final String TASK_ID_PATH = "/{taskId}";
    private static final String COMMENTS_PATH = "/comments";
    private static final String PRIORITY_PATH = "/priority";
    private static final String STATUS_PATH = "/status";
    private static final String ASSIGN_PATH = "/assign";
    private static final String ADMIN_ALL_PATH = "/admin/all";

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    @Operation(
            summary = "Получить задачи по автору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Автор не найден")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(AUTHOR_PATH)
    public ResponseEntity<List<TaskDTO>> getTasksByAuthor(
            @Parameter(description = "ID автора задачи") @PathVariable Long authorId) {
        List<TaskDTO> tasks = taskService.getTasksByAuthor(authorId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Получить задачи по исполнителю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Исполнитель не найден")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(ASSIGNEE_PATH)
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(
            @PathVariable Long assigneeId) {
        List<TaskDTO> tasks = taskService.getTasksByAssignee(assigneeId);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Создать новую задачу",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(CREATE_PATH)
    public ResponseEntity<TaskDTO> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(TASK_ID_PATH)
    public ResponseEntity<TaskDTO> updateTask(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
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
            responses = {
                    @ApiResponse(responseCode = "201", description = "Комментарий успешно добавлен"),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping(TASK_ID_PATH + COMMENTS_PATH)
    public ResponseEntity<CommentDTO> addComment(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        User author = (User) authentication.getPrincipal();
        CommentDTO commentDTO = taskService.addComment(taskId, request.getText(), author);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDTO);
    }

    @Operation(
            summary = "Изменить приоритет задачи",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Приоритет задачи успешно обновлен"),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(TASK_ID_PATH + PRIORITY_PATH)
    public ResponseEntity<TaskDTO> updateTaskPriority(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
            @Valid @RequestBody UpdatePriorityRequest request) {
        TaskDTO taskDTO = taskService.updateTaskPriority(taskId, request.getPriority());
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(
            summary = "Обновить статус задачи",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус задачи успешно обновлен"),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PutMapping(TASK_ID_PATH + STATUS_PATH)
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        TaskDTO taskDTO = taskService.updateTaskStatus(taskId, request.getStatus(), currentUser);
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(
            summary = "Удалить задачу",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(TASK_ID_PATH)
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID задачи") @PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Назначить задачу исполнителю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача успешно назначена"),
                    @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Задача или исполнитель не найдены")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping(TASK_ID_PATH + ASSIGN_PATH + "/{assigneeId}")
    public ResponseEntity<TaskDTO> assignTask(
            @Parameter(description = "ID задачи") @PathVariable Long taskId,
            @Parameter(description = "ID исполнителя") @PathVariable Long assigneeId) {

        logger.info("Assigning task ID {} to assignee ID {}", taskId, assigneeId);

        try {
            TaskDTO taskDTO = taskService.assignTask(taskId, assigneeId);
            return ResponseEntity.ok(taskDTO);
        } catch (TaskNotFoundException | UserNotFoundException ex) {
            logger.error("Assignment error: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @Operation(
            summary = "Получить задачи с фильтрацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены"),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
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
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задачи успешно найдены"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(ADMIN_ALL_PATH)
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TaskDTO> tasks = taskService.getAllTasks(page, size);
        return ResponseEntity.ok(tasks);
    }
}