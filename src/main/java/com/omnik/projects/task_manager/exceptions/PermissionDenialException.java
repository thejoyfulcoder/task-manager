package com.omnik.projects.task_manager.exceptions;

public class PermissionDenialException extends RuntimeException{

    public PermissionDenialException(){
        super("The requester user has no permission to perform the required operation");
    }

    public PermissionDenialException(String message){
        super(message);
    }
}
