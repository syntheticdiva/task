package com.example.task.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.task.dto.TaskDTO;
import com.example.task.entity.Task;
import com.example.task.entity.User;
import com.example.task.enums.TaskPriority;
import com.example.task.enums.TaskStatus;
import com.example.task.exception.*;
import com.example.task.mapper.TaskMapper;
import com.example.task.repository.TaskRepository;
import com.example.task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User author;
    private User assignee;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);

        assignee = new User();
        assignee.setId(2L);

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIUM);
        task.setAuthor(author);
        task.setAssignee(assignee);

        taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("Test Task");
        taskDTO.setDescription("Test Description");
        taskDTO.setStatus(TaskStatus.PENDING);
        taskDTO.setPriority(TaskPriority.MEDIUM);
        taskDTO.setAuthorId(author.getId());
        taskDTO.setAssigneeId(assignee.getId());
    }

    @Test
    void getTasksByAuthor_ValidAuthorId_ReturnsTaskDTOList() {
        // Arrange
        when(userRepository.existsById(author.getId())).thenReturn(true);
        when(taskRepository.findByAuthorId(author.getId())).thenReturn(Collections.singletonList(task));
        when(taskMapper.toTaskDTO(task)).thenReturn(taskDTO);

        // Act
        List<TaskDTO> result = taskService.getTasksByAuthor(author.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskDTO, result.get(0));
        verify(userRepository, times(1)).existsById(author.getId());
        verify(taskRepository, times(1)).findByAuthorId(author.getId());
    }

    @Test
    void getTasksByAuthor_InvalidAuthorId_ThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.existsById(author.getId())).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            taskService.getTasksByAuthor(author.getId());
        });
    }

    @Test
    void createTask_ValidInput_ReturnsTaskDTO() {
        // Arrange
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toTaskDTO(task)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.createTask(
                "Test Task",
                "Test Description",
                TaskStatus.PENDING,
                TaskPriority.MEDIUM,
                author,
                assignee.getId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(taskDTO, result);
        verify(userRepository, times(1)).findById(assignee.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_InvalidAssigneeId_ThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            taskService.createTask(
                    "Test Task",
                    "Test Description",
                    TaskStatus.PENDING,
                    TaskPriority.MEDIUM,
                    author,
                    assignee.getId()
            );
        });
    }

    @Test
    void updateTask_ValidInput_ReturnsUpdatedTaskDTO() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toTaskDTO(task)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.updateTask(
                task.getId(),
                "Updated Task",
                "Updated Description",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                assignee.getId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(taskDTO, result);
        verify(taskRepository, times(1)).findById(task.getId());
        verify(userRepository, times(1)).findById(assignee.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_InvalidTaskId_ThrowsTaskNotFoundException() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(
                    task.getId(),
                    "Updated Task",
                    "Updated Description",
                    TaskStatus.IN_PROGRESS,
                    TaskPriority.HIGH,
                    assignee.getId()
            );
        });
    }

    @Test
    void deleteTask_ValidTaskId_DeletesTask() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        // Act
        taskService.deleteTask(task.getId());

        // Assert
        verify(taskRepository, times(1)).findById(task.getId());
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_InvalidTaskId_ThrowsTaskNotFoundException() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(task.getId());
        });
    }

    @Test
    void assignTask_ValidInput_ReturnsAssignedTaskDTO() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toTaskDTO(task)).thenReturn(taskDTO);

        // Act
        TaskDTO result = taskService.assignTask(task.getId(), assignee.getId());

        // Assert
        assertNotNull(result);
        assertEquals(taskDTO, result);
        verify(taskRepository, times(1)).findById(task.getId());
        verify(userRepository, times(1)).findById(assignee.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void assignTask_InvalidTaskId_ThrowsTaskNotFoundException() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.assignTask(task.getId(), assignee.getId());
        });
    }

    @Test
    void assignTask_InvalidAssigneeId_ThrowsUserNotFoundException() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            taskService.assignTask(task.getId(), assignee.getId());
        });
    }

    @Test
    void getTasks_ValidFilters_ReturnsTaskDTOPage() {
        // Arrange
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(taskPage);
        when(taskMapper.toTaskDTO(task)).thenReturn(taskDTO);

        // Act
        Page<TaskDTO> result = taskService.getTasks(
                TaskStatus.PENDING,
                TaskPriority.MEDIUM,
                author.getId(),
                assignee.getId(),
                0,
                10
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTasks_NoFilters_ThrowsInvalidRequestException() {
        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            taskService.getTasks(null, null, null, null, 0, 10);
        });
    }

    @Test
    void getAllTasks_ValidPageAndSize_ReturnsTaskDTOPage() {
        // Arrange
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(taskPage);
        when(taskMapper.toTaskDTO(task)).thenReturn(taskDTO);

        // Act
        Page<TaskDTO> result = taskService.getAllTasks(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));
        verify(taskRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getAllTasks_InvalidPage_ThrowsInvalidRequestException() {
        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            taskService.getAllTasks(-1, 10);
        });
    }

    @Test
    void getAllTasks_InvalidSize_ThrowsInvalidRequestException() {
        // Act & Assert
        assertThrows(InvalidRequestException.class, () -> {
            taskService.getAllTasks(0, 0);
        });
    }
}