package com.omnik.projects.task_manager.controller.impl;

import com.omnik.projects.task_manager.controller.TaskController;
import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskControllerImpl implements TaskController {

    private final TaskService taskService;

    public TaskControllerImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> createTask(String requesterUsername, TaskRequestDTO taskCreationRequest) {
        return ResponseEntity.ok(taskService.createTask(requesterUsername,taskCreationRequest));
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> createTask(String requesterUsername, String assigneeUsername, TaskRequestDTO taskCreationRequest) {
        return ResponseEntity.ok(taskService.assignTask(requesterUsername,assigneeUsername,taskCreationRequest));

    }
}
