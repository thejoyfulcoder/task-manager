package com.omnik.projects.task_manager.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(){
        super("Username already taken!");
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
