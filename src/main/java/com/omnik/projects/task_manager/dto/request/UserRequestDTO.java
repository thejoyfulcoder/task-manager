package com.omnik.projects.task_manager.dto.request;

import com.omnik.projects.task_manager.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRequestDTO {
    private String username;
    private String firstName;
    private String lastName;
    private Role role;
}
