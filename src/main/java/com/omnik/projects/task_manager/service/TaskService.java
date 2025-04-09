package com.omnik.projects.task_manager.service;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.enums.TaskStatus;
import org.springframework.stereotype.Service;

@Service
public interface TaskService {

    ApiResponseDTO<?> createTask(String requesterUsername, boolean scheduleTask,boolean bufferTask,TaskRequestDTO taskCreationRequest);

    ApiResponseDTO<?> deleteTask(String requesterUsername, String taskName);

    ApiResponseDTO<?> assignTask(String requesterUsername, String assigneeUsername, boolean scheduleTask, boolean bufferTask, TaskRequestDTO taskCreationRequest);

    ApiResponseDTO<?> scheduleTask(String taskName);

    ApiResponseDTO<?> bufferTask(String taskName);

    ApiResponseDTO<?> processTask(String requesterUsername, String taskName, TaskStatus status);

    ApiResponseDTO<?> markAsCompleted(String requesterUsername, String taskName);
}
