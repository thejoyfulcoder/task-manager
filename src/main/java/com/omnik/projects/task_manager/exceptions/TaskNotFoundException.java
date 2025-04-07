package com.omnik.projects.task_manager.exceptions;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(){
        super("Task not found!!");
    }
    public TaskNotFoundException(String message) {
        super(message);
    }
}
