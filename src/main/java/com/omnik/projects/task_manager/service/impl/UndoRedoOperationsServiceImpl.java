package com.omnik.projects.task_manager.service.impl;

import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.history.Operations;
import com.omnik.projects.task_manager.exceptions.RedoStackEmptyException;
import com.omnik.projects.task_manager.exceptions.UndoStackEmptyException;
import com.omnik.projects.task_manager.service.UndoRedoOperationsService;
import com.omnik.projects.task_manager.storage.DataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class UndoRedoOperationsServiceImpl implements UndoRedoOperationsService {

    private final DataStore dataStore;

    public UndoRedoOperationsServiceImpl(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public ApiResponseDTO<?> undoOperation() {
        try {
            Operations operationToBeUndone =dataStore.popOperationToUndo();
            operationToBeUndone.revert();
            dataStore.addNewOperationToRedoStack(operationToBeUndone);
            return new ApiResponseDTO<>(HttpStatus.OK,"Operation undone successfully",false);
        }catch ( UndoStackEmptyException e){
            return new ApiResponseDTO<>(HttpStatus.CONFLICT, e.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }

    @Override
    public ApiResponseDTO<?> redoOperation() {
        try {
            Operations operationToBeRedone =dataStore.popOperationToRedo();
            operationToBeRedone.apply();
            dataStore.addNewOperationToUndoStack(operationToBeRedone);
            return new ApiResponseDTO<>(HttpStatus.OK,"Operation redone successfully",false);
        }catch ( RedoStackEmptyException e){
            return new ApiResponseDTO<>(HttpStatus.CONFLICT, e.getMessage(),true);
        }catch (Exception e){
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),true);
        }
    }
}
