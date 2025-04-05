package com.omnik.projects.task_manager.exceptions;

public class UserNotFoundException extends RuntimeException
{
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException() {
        super("User Not Found");
    }
}
