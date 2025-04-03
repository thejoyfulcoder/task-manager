package com.omnik.projects.task_manager.exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(){
        super("Username already taken!");
    }

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
