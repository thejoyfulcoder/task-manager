package com.omnik.projects.task_manager.service;

import com.omnik.projects.task_manager.dto.request.UserRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.enums.Permission;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    ApiResponseDTO<?> addUser( String requestUsername,UserRequestDTO userCreationRequest);

    User validateUser(String requesterUsername, Permission permission);

    boolean hasPermission(User userFromDataStore,Permission permission);

    }
