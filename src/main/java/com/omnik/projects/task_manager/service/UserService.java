package com.omnik.projects.task_manager.service;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.request.UserRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    ApiResponseDTO<?> addUser( String requestUsername,UserRequestDTO userCreationRequest);

    ApiResponseDTO<?> createTask( String requesterUsername, TaskRequestDTO taskCreationRequest);

}
