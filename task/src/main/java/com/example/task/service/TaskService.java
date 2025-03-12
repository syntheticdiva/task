package com.example.task.service;


import com.example.task.dto.CommentDTO;
import com.example.task.dto.TaskDTO;
import com.example.task.entity.Comment;
import com.example.task.entity.Task;
import com.example.task.entity.User;
import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import com.example.task.exception.InvalidRequestException;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.exception.UnauthorizedActionException;
import com.example.task.exception.UserNotFoundException;
import com.example.task.mapper.CommentMapper;
import com.example.task.mapper.TaskMapper;
import com.example.task.repository.CommentRepository;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private static final int MIN_ID_VALUE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final TaskMapper taskMapper;
    private final CommentMapper commentMapper;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, CommentRepository commentRepository, TaskMapper taskMapper, CommentMapper commentMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.taskMapper = taskMapper;
        this.commentMapper = commentMapper;
    }

    public List<TaskDTO> getTasksByAuthor(@NotNull @Min(MIN_ID_VALUE) Long authorId) {
        logger.info("Attempting to find tasks for author ID: {}", authorId);

        if (!userRepository.existsById(authorId)) {
            logger.error("Author with ID {} not found", authorId);
            throw new UserNotFoundException("Автор с ID " + authorId + " не найден");
        }

        List<Task> tasks = taskRepository.findByAuthorId(authorId);
        logger.debug("Found {} tasks for author ID: {}", tasks.size(), authorId);

        return tasks.stream()
                .map(taskMapper::toTaskDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByAssignee(Long assigneeId) {
        if (!userRepository.existsById(assigneeId)) {
            throw new UserNotFoundException("Исполнитель с ID " + assigneeId + " не найден");
        }

        return taskRepository.findByAssigneeId(assigneeId)
                .stream()
                .map(taskMapper::toTaskDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public TaskDTO createTask(
            @NotNull String title,
            @NotNull String description,
            TaskStatus status,
            TaskPriority priority,
            @NotNull User author,
            @NotNull Long assigneeId
    ) {
        logger.info("Creating task with title: {}", title);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new UserNotFoundException("Assignee not found with id: " + assigneeId));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status != null ? status : TaskStatus.PENDING);
        task.setPriority(priority != null ? priority : TaskPriority.MEDIUM);
        task.setAuthor(author);
        task.setAssignee(assignee);

        Task savedTask = taskRepository.save(task);
        return taskMapper.toTaskDTO(savedTask);
    }

    public TaskDTO updateTask(
            @NotNull Long taskId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId
    ) {
        logger.info("Updating task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null) {
            task.setStatus(status);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new UserNotFoundException("Assignee not found with id: " + assigneeId));
            task.setAssignee(assignee);
        }

        Task updatedTask = taskRepository.save(task);
        return taskMapper.toTaskDTO(updatedTask);
    }

    public void deleteTask(@NotNull Long taskId) {
        logger.info("Deleting task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        taskRepository.delete(task);
    }

    public TaskDTO assignTask(
            @NotNull(message = "Task ID cannot be null") Long taskId,
            @NotNull(message = "Assignee ID cannot be null") Long assigneeId) {

        logger.info("Assigning task with ID: {} to assignee with ID: {}", taskId, assigneeId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.error("Task not found with ID: {}", taskId);
                    return new TaskNotFoundException("Task not found with id: " + taskId);
                });

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> {
                    logger.error("Assignee not found with ID: {}", assigneeId);
                    return new UserNotFoundException("Assignee not found with id: " + assigneeId);
                });

        task.setAssignee(assignee);
        Task assignedTask = taskRepository.save(task);

        logger.info("Task ID: {} successfully assigned to user ID: {}", taskId, assigneeId);
        return taskMapper.toTaskDTO(assignedTask);
    }
    public Page<TaskDTO> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long authorId,
            Long assigneeId,
            int page,
            int size
    ) {
        logger.info("Fetching tasks with filters: status={}, priority={}, authorId={}, assigneeId={}, page={}, size={}",
                status, priority, authorId, assigneeId, page, size);

        validatePageAndSize(page, size);

        if (status == null && priority == null && authorId == null && assigneeId == null) {
            throw new InvalidRequestException("At least one filter parameter must be provided");
        }

        Specification<Task> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (authorId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("author").get("id"), authorId));
        }
        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }

        Page<Task> tasks = taskRepository.findAll(spec, PageRequest.of(page, size));

        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("No tasks found with the specified filters");
        }

        return tasks.map(taskMapper::toTaskDTO);
    }

    public CommentDTO addComment(@NotNull Long taskId, @NotNull String text, @NotNull User author) {
        logger.info("Adding comment to task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (!task.getAuthor().getId().equals(author.getId())
                && !task.getAssignee().getId().equals(author.getId())) {
            throw new UnauthorizedActionException("You are not authorized to comment on this task");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setTask(task);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDTO(savedComment);
    }

    @Transactional
    public TaskDTO updateTaskPriority(@NotNull Long taskId, @NotNull TaskPriority priority) {
        logger.info("Updating priority of task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        task.setPriority(priority);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toTaskDTO(updatedTask);
    }

    public TaskDTO updateTaskStatus(@NotNull Long taskId, @NotNull TaskStatus status, @NotNull User currentUser) {
        logger.info("Updating status of task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (currentUser.hasUserRole() &&
                !task.getAssignee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No permission to update status");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toTaskDTO(updatedTask);
    }

    public Page<TaskDTO> getAllTasks(int page, int size) {
        logger.info("Fetching all tasks with page={}, size={}", page, size);
        validatePageAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskRepository.findAll(pageable);

        return tasks.map(taskMapper::toTaskDTO);
    }

    private void validatePageAndSize(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("Page number must not be less than zero");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("Page size must be between 1 and 100");
        }
    }
}