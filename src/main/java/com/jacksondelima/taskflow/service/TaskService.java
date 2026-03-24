package com.jacksondelima.taskflow.service;

import com.jacksondelima.taskflow.dto.task.TaskRequestDTO;
import com.jacksondelima.taskflow.dto.task.TaskResponseDTO;
import com.jacksondelima.taskflow.entity.Task;
import com.jacksondelima.taskflow.entity.User;
import com.jacksondelima.taskflow.enums.TaskStatus;
import com.jacksondelima.taskflow.repository.TaskRepository;
import com.jacksondelima.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponseDTO createTask(TaskRequestDTO request) {
        User authenticatedUser = getAuthenticatedUser();

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.PENDING)
                .user(authenticatedUser)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    public List<TaskResponseDTO> getMyTasks() {
        User authenticatedUser = getAuthenticatedUser();

        return taskRepository.findByUser(authenticatedUser)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponseDTO getTaskById(Long id) {
        User user = getAuthenticatedUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        return mapToResponse(task);
    }

    public TaskResponseDTO updateTask(Long id, TaskRequestDTO request) {
        User user = getAuthenticatedUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        task.setTitle(request.title());
        task.setDescription(request.description());

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    public void deleteTask(Long id) {
        User user = getAuthenticatedUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        taskRepository.delete(task);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
    }

    private TaskResponseDTO mapToResponse(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getUser().getEmail()
        );
    }
}