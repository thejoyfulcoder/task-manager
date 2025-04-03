package com.omnik.projects.task_manager.entities;

import com.omnik.projects.task_manager.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private String username;
    private String firstName;
    private String lastName;
    private Set<Role> roles;

    public User(String username,String firstName,String lastName){
        this.username= username;
        this.firstName=firstName;
        this.lastName=lastName;
        this.roles=new HashSet<Role>();
    }
}
