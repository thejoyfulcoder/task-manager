package com.omnik.projects.task_manager.exceptions;

public class RedoStackEmptyException extends RuntimeException {

    public RedoStackEmptyException() {
        super("Nothing to redo – you're all caught up!");
    }

    public RedoStackEmptyException(String message) {
        super(message);
    }

}
