package com.example.task.service;



import com.example.task.entity.Comment;
import com.example.task.entity.Task;
import com.example.task.entity.User;
import com.example.task.enums.Role;
import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
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

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;

    public List<Task> getTasksByAuthor(Long authorId) {
        return taskRepository.findByAuthorId(authorId);
    }

    public List<Task> getTasksByAssignee(Long assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId);
    }


    public Task createTask(
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            User author,
            Long assigneeId
    ) {
        // Находим исполнителя по ID
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));

        // Создаем задачу
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
                .orElseThrow(() -> new RuntimeException("Task not found"));

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
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
    }
    public Task assignTask(Long taskId, Long assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));

        task.setAssignee(assignee);
        return taskRepository.save(task);
    }

    public Page<Task> getTasks(
            TaskStatus status,
            TaskPriority priority,
            Long authorId,
            Long assigneeId,
            int page,
            int size
    ) throws BadRequestException {
        // Валидация пагинации
        if (page < 0) {
            throw new BadRequestException("Page number must not be less than zero");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }

        // Проверка критериев фильтрации
        if (status == null && priority == null && authorId == null && assigneeId == null) {
            throw new BadRequestException("At least one filter parameter must be provided");
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

        return taskRepository.findAll(spec, PageRequest.of(page, size));
    }

    public Comment addComment(Long taskId, String text, User author) {
        // Находим задачу по ID
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Проверяем, что пользователь имеет право оставлять комментарий
        if (!task.getAuthor().getId().equals(author.getId())){
            throw new RuntimeException("You are not authorized to comment on this task");
        }

        // Создаем комментарий
        Comment comment = new Comment();
        comment.setText(text);
        comment.setTask(task);
        comment.setAuthor(author);

        // Сохраняем комментарий
        return commentRepository.save(comment);
    }

    public Task updateTaskStatus(Long taskId, TaskStatus status, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Для USER проверяем, что он исполнитель
        if (currentUser.hasUserRole() &&
                !task.getAssignee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No permission to update status");
        }

        task.setStatus(status);
        return taskRepository.save(task);
    }
//    public Task updateTaskStatus(Long taskId, TaskStatus status, User currentUser) {
//
//        Task task = taskRepository.findById(taskId)
//                .orElseThrow(() -> new RuntimeException("Task not found"));
//
//        if (!currentUser.getRoles().contains(Role.ROLE_ADMIN)) {
//            if (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId())) {
//                throw new RuntimeException("You are not authorized to update the status of this task");
//            }
//        }
//
//        task.setStatus(status);
//
//        return taskRepository.save(task);
//    }
}



