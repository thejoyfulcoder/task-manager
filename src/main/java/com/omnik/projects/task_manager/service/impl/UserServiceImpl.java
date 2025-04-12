package com.omnik.projects.task_manager.service.impl;

import com.omnik.projects.task_manager.dto.request.UserRequestDTO;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.User;
import com.omnik.projects.task_manager.enums.Permission;
import com.omnik.projects.task_manager.enums.Role;
import com.omnik.projects.task_manager.exceptions.PermissionDenialException;
import com.omnik.projects.task_manager.exceptions.UserAlreadyExistsException;
import com.omnik.projects.task_manager.exceptions.UserNotFoundException;
import com.omnik.projects.task_manager.service.UserService;
import com.omnik.projects.task_manager.storage.DataStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserServiceImpl implements UserService {

    private final DataStore dataStore;

    @Override
    public ApiResponseDTO<?> addUser(String requesterUsername, UserRequestDTO userCreationRequest) {
        log.info("Adding new user: {} by requester: {}", userCreationRequest.getUsername(), requesterUsername);
        try {
            log.debug("Validating requester: {} for user creation permission", requesterUsername);
            validateUser(requesterUsername, Permission.Create_User);

            log.debug("Creating new user with username: {}", userCreationRequest.getUsername());
            User newUser = new User(userCreationRequest.getUsername(), userCreationRequest.getLastName(), userCreationRequest.getLastName());
            newUser.getRoles().add(userCreationRequest.getRole());

            log.debug("Adding user to data store");
            dataStore.addNewUser(newUser);

            log.info("User {} created successfully by requester: {}", userCreationRequest.getUsername(), requesterUsername);
            return new ApiResponseDTO<>(HttpStatus.OK, "User created successfully", false);
        } catch (PermissionDenialException e) {
            log.error("Permission error while adding user: {}. Error: {}", userCreationRequest.getUsername(), e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(), true);
        } catch (UserNotFoundException nfe) {
            log.error("Requester not found while adding user: {}. Error: {}", userCreationRequest.getUsername(), nfe.getMessage());
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(), true);
        } catch (UserAlreadyExistsException e) {
            log.error("User already exists: {}. Error: {}", userCreationRequest.getUsername(), e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.CONFLICT, e.getMessage(), true);
        } catch (Exception e) {
            log.error("Unexpected error while adding user: {}. Error: {}", userCreationRequest.getUsername(), e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), true);
        }
    }

    @Override
    public User validateUser(String requesterUsername, Permission permission) {
        log.info("Validating user for username: {}", requesterUsername);
        try {
            log.debug("Retrieving user: {} from data store", requesterUsername);
            User userFromDataStore = dataStore.getAllUsers().get(requesterUsername);

            if (userFromDataStore == null) {
                log.warn("User not found: {}", requesterUsername);
                throw new UserNotFoundException("No user exists for the requester User: " + requesterUsername);
            }

            if (permission != null) {
                log.debug("Checking permission: {} for user: {}", permission, requesterUsername);
                if (!hasPermission(userFromDataStore, permission)) {
                    log.warn("Permission {} denied for user: {}", permission, requesterUsername);
                    throw new PermissionDenialException();
                }
            }

            log.info("User {} validated successfully", requesterUsername);
            return userFromDataStore;
        } catch (Exception e) {
            log.error("Error validating user: {}. Error: {}", requesterUsername, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean hasPermission(User userFromDataStore, Permission permission) {
        log.debug("Checking if user: {} has permission: {}", userFromDataStore.getUsername(), permission);
        Map<Role, Set<Permission>> rolePermissions = dataStore.getAllRolePermissions();
        Set<Role> userRoles = userFromDataStore.getRoles();

        for (Role role : userRoles) {
            log.trace("Checking role: {} for permission: {}", role, permission);
            Set<Permission> permissions = rolePermissions.get(role);
            if (permissions != null && permissions.contains(permission)) {
                log.debug("Permission {} granted for user: {} via role: {}", permission, userFromDataStore.getUsername(), role);
                return true;
            }
        }

        log.debug("Permission {} not granted for user: {}", permission, userFromDataStore.getUsername());
        return false;
    }
}