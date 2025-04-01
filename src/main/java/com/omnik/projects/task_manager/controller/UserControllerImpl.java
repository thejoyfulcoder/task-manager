package com.omnik.projects.task_manager.controller;

import com.omnik.projects.task_manager.dto.request.TaskRequestDTO;
import com.omnik.projects.task_manager.dto.request.UserRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserControllerImpl implements UserController{

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<ApiResponseDTO<?>> addUser(String requesterUsername, UserRequestDTO userCreationRequest) {
        return ResponseEntity.ok(userService.addUser(requesterUsername,userCreationRequest));
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> createTask(String requesterUsername, TaskRequestDTO taskCreationRequest) {
        return ResponseEntity.ok(userService.createTask(requesterUsername,taskCreationRequest));
    }
}
