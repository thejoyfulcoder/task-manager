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
    public ResponseEntity<ApiResponseDTO<?>> createTask(String requesterUsername, boolean scheduleTask,boolean bufferTask, TaskRequestDTO taskCreationRequest) {
        return ResponseEntity.ok(taskService.createTask(requesterUsername,scheduleTask,bufferTask,taskCreationRequest));
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> assignTask(String requesterUsername, String assigneeUsername,  boolean scheduleTask, boolean bufferTask,TaskRequestDTO taskCreationRequest) {
        return ResponseEntity.ok(taskService.assignTask(requesterUsername,assigneeUsername,scheduleTask,bufferTask,taskCreationRequest));

    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> scheduleTask(String taskName) {
        return ResponseEntity.ok(taskService.scheduleTask(taskName));
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> bufferTask(String taskName) {
        return ResponseEntity.ok(taskService.bufferTask(taskName));
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> deleteTask(String requesterUsername, String taskName) {
        return ResponseEntity.ok(taskService.deleteTask(requesterUsername,taskName));
    }
}
