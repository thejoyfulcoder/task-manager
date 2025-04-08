package com.omnik.projects.task_manager.entities.history;

import com.omnik.projects.task_manager.entities.Task;
import com.omnik.projects.task_manager.storage.DataStore;

public class TaskCreationAndDeletionOperation extends Operations {

    private final Task task;
    private final boolean scheduleTask;
    private final boolean bufferTask;
    private final boolean isCreationOperation;

    public TaskCreationAndDeletionOperation(DataStore dataStore, Task task, boolean scheduleTask, boolean bufferTask, boolean isCreationOperation) {
        super(dataStore);
        this.task = task;
        this.scheduleTask = scheduleTask;
        this.bufferTask = bufferTask;
        this.isCreationOperation = isCreationOperation;
    }

    @Override
    public void apply() {
        if(isCreationOperation){
            createTask();
        }else {
            deleteTask();
        }
    }

    @Override
    public void revert() {
        if(isCreationOperation){
            deleteTask();
        }else {
           createTask();
        }
    }

    private void createTask(){
        dataStore.addNewUserTask(task);
        if(scheduleTask) {
            dataStore.scheduleTask(task);
        } else if (bufferTask) {
            dataStore.bufferTask(task);
        }
    }

    private void deleteTask(){
        dataStore.deleteTask(task);
    }
}
