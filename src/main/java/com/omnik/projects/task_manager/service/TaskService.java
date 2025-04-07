package com.omnik.projects.task_manager.service;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface TaskService {

    ApiResponseDTO<?> createTask(String requesterUsername, boolean scheduleTask,boolean bufferTask,TaskRequestDTO taskCreationRequest);

    ApiResponseDTO<?> assignTask(String requesterUsername, String assigneeUsername, boolean scheduleTask,boolean bufferTask, TaskRequestDTO taskCreationRequest);

    ApiResponseDTO<?> scheduleTask(String taskName);

    ApiResponseDTO<?> bufferTask(String taskName);
}
