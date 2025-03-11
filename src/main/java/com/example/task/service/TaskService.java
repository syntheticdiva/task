package com.example.task.service;



import com.example.task.dto.CommentDTO;
import com.example.task.dto.TaskDTO;
import com.example.task.entity.Comment;
import com.example.task.entity.Task;
import com.example.task.entity.User;
import com.example.task.enums.Role;
import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import com.example.task.exception.InvalidRequestException;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.exception.UnauthorizedActionException;
import com.example.task.exception.UserNotFoundException;
import com.example.task.repository.CommentRepository;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;

    public List<TaskDTO> getTasksByAuthor(Long authorId) {
        List<Task> tasks = taskRepository.findByAuthorId(authorId);

        // Преобразуем список сущностей Task в список TaskDTO
        return tasks.stream()
                .map(task -> {
                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setId(task.getId());
                    taskDTO.setTitle(task.getTitle());
                    taskDTO.setDescription(task.getDescription());
                    taskDTO.setStatus(task.getStatus());
                    taskDTO.setPriority(task.getPriority());
                    taskDTO.setAuthorId(task.getAuthor().getId());
                    taskDTO.setAssigneeId(task.getAssignee().getId());

                    // taskDTO.setComments(convertCommentsToDTOs(task.getComments()));

                    return taskDTO;
                })
                .collect(Collectors.toList());
    }
    public List<TaskDTO> getTasksByAssignee(Long assigneeId) {
        List<Task> tasks = taskRepository.findByAssigneeId(assigneeId);

        // Преобразуем список сущностей Task в список TaskDTO
        return tasks.stream()
                .map(task -> {
                    TaskDTO taskDTO = new TaskDTO();
                    taskDTO.setId(task.getId());
                    taskDTO.setTitle(task.getTitle());
                    taskDTO.setDescription(task.getDescription());
                    taskDTO.setStatus(task.getStatus());
                    taskDTO.setPriority(task.getPriority());
                    taskDTO.setAuthorId(task.getAuthor().getId());
                    taskDTO.setAssigneeId(task.getAssignee().getId());

                    // Если нужно передавать комментарии
                    // taskDTO.setComments(convertCommentsToDTOs(task.getComments()));

                    return taskDTO;
                })
                .collect(Collectors.toList());
    }

    public Task createTask(
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            User author,
            Long assigneeId
    ) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new UserNotFoundException("Assignee not found with id: " + assigneeId));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setAuthor(author);
        task.setAssignee(assignee);

        return taskRepository.save(task);
    }

    public Task updateTask(
            Long taskId,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId
    ) {
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

        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        taskRepository.delete(task);
    }

    public TaskDTO assignTask(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new UserNotFoundException("Assignee not found with id: " + assigneeId));

        task.setAssignee(assignee);
        Task assignedTask = taskRepository.save(task);

        // Преобразуем сущность Task в TaskDTO
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(assignedTask.getId());
        taskDTO.setTitle(assignedTask.getTitle());
        taskDTO.setDescription(assignedTask.getDescription());
        taskDTO.setStatus(assignedTask.getStatus());
        taskDTO.setPriority(assignedTask.getPriority());
        taskDTO.setAuthorId(assignedTask.getAuthor().getId());
        taskDTO.setAssigneeId(assignedTask.getAssignee().getId());

        // Если нужно передавать комментарии
        // taskDTO.setComments(convertCommentsToDTOs(assignedTask.getComments()));

        return taskDTO;
    }
    public Page<TaskDTO> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long authorId,
            Long assigneeId,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new InvalidRequestException("Page number must not be less than zero");
        }

        if (size < 1 || size > 100) {
            throw new InvalidRequestException("Page size must be between 1 and 100");
        }

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

        // Проверяем, есть ли задачи
        if (tasks.isEmpty()) {
            throw new TaskNotFoundException("No tasks found with the specified filters");
        }

        // Преобразуем Page<Task> в Page<TaskDTO>
        return tasks.map(task -> {
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setId(task.getId());
            taskDTO.setTitle(task.getTitle());
            taskDTO.setDescription(task.getDescription());
            taskDTO.setStatus(task.getStatus());
            taskDTO.setPriority(task.getPriority());
            taskDTO.setAuthorId(task.getAuthor().getId());
            taskDTO.setAssigneeId(task.getAssignee().getId());

            // Если нужно передавать комментарии
            // taskDTO.setComments(convertCommentsToDTOs(task.getComments()));

            return taskDTO;
        });
    }

    public CommentDTO addComment(Long taskId, String text, User author) {
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

        // Преобразуем сущность Comment в CommentDTO
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(savedComment.getId());
        commentDTO.setText(savedComment.getText());
        commentDTO.setTaskId(savedComment.getTask().getId());
        commentDTO.setAuthorId(savedComment.getAuthor().getId());

        return commentDTO;
    }
    public TaskDTO updateTaskStatus(Long taskId, TaskStatus status, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (currentUser.hasUserRole() &&
                !task.getAssignee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No permission to update status");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        // Преобразуем сущность Task в TaskDTO
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(updatedTask.getId());
        taskDTO.setTitle(updatedTask.getTitle());
        taskDTO.setDescription(updatedTask.getDescription());
        taskDTO.setStatus(updatedTask.getStatus());
        taskDTO.setPriority(updatedTask.getPriority());
        taskDTO.setAuthorId(updatedTask.getAuthor().getId());
        taskDTO.setAssigneeId(updatedTask.getAssignee().getId());

        // Если нужно передавать комментарии
        // taskDTO.setComments(convertCommentsToDTOs(updatedTask.getComments()));

        return taskDTO;
    }
}