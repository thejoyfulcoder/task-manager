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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class UserServiceImpl implements UserService {

    private final DataStore dataStore;

    @Override
    public ApiResponseDTO<?> addUser(String requesterUsername, UserRequestDTO userCreationRequest) {
        try {
          validateUser(requesterUsername,Permission.Create_User);
          User newUser = new User(userCreationRequest.getUsername(),userCreationRequest.getLastName(),userCreationRequest.getLastName());
          newUser.getRoles().add(userCreationRequest.getRole());
          dataStore.addNewUser(newUser);
          return new ApiResponseDTO<>(HttpStatus.OK,"User created successfully",false);
        }catch (PermissionDenialException e){
            return new ApiResponseDTO<>(HttpStatus.BAD_REQUEST, e.getMessage(),true);
        }catch (UserNotFoundException nfe){
            return new ApiResponseDTO<>(HttpStatus.NOT_FOUND, nfe.getMessage(),true);
        }catch (UserAlreadyExistsException e){
            return new ApiResponseDTO<>(HttpStatus.CONFLICT,e.getMessage(),true);
        } catch (Exception e){
             return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public User validateUser(String requesterUsername, Permission permission) {
        User userFromDataStore = dataStore.getUsers().stream().filter(user -> user.getUsername().equals(requesterUsername)).findFirst().orElse(null);
        if(userFromDataStore==null){
            throw new UserNotFoundException("No user exists for the requester User: "+requesterUsername);
        }
        if(permission != null && !hasPermission(userFromDataStore,permission)){
            throw new PermissionDenialException();
        }
        return userFromDataStore;
    }


    @Override
    public boolean hasPermission(User userFromDataStore,Permission permission) {
        Map<Role, Set<Permission>> rolePermissions= dataStore.getAllRolePermissions();
        Set<Role> userRoles = userFromDataStore.getRoles();
        for(Role role: userRoles){
            Set<Permission> permissions= rolePermissions.get(role);
            if(permissions!= null && permissions.contains(permission)){
                return true;
            }
        }
        return false;
    }



}
