package com.example.task.controller;

import com.example.task.entity.Task;
import com.example.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "API для управления задачами")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Получить задачи по автору", description = "Возвращает список задач по ID автора")
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Task>> getTasksByAuthor(
            @Parameter(description = "ID автора задачи") @PathVariable Long authorId) {
        return ResponseEntity.ok(taskService.getTasksByAuthor(authorId));
    }

    @Operation(summary = "Получить задачи по исполнителю", description = "Возвращает список задач по ID исполнителя")
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<Task>> getTasksByAssignee(
            @Parameter(description = "ID исполнителя задачи") @PathVariable Long assigneeId) {
        return ResponseEntity.ok(taskService.getTasksByAssignee(assigneeId));
    }

    @Operation(summary = "Создать новую задачу")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Task> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные новой задачи")
            @RequestBody Task task) {
        return new ResponseEntity<>(taskService.createTask(task), HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить существующую задачу")
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "ID задачи для обновления") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Новые данные задачи")
            @RequestBody Task taskDetails) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDetails));
    }

    @Operation(summary = "Удалить задачу")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID задачи для удаления") @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}