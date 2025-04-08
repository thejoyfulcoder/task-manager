package com.omnik.projects.task_manager.exceptions;

public class UndoStackEmptyException extends RuntimeException {

    public UndoStackEmptyException() {
        super("Nothing to undo – you're all caught up!");
    }

    public UndoStackEmptyException(String message) {
        super(message);
    }
}
