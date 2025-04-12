package com.omnik.projects.task_manager.service.impl;

import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.entities.history.Operations;
import com.omnik.projects.task_manager.exceptions.RedoStackEmptyException;
import com.omnik.projects.task_manager.exceptions.UndoStackEmptyException;
import com.omnik.projects.task_manager.service.UndoRedoOperationsService;
import com.omnik.projects.task_manager.storage.DataStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UndoRedoOperationsServiceImpl implements UndoRedoOperationsService {

    private final DataStore dataStore;

    public UndoRedoOperationsServiceImpl(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public ApiResponseDTO<?> undoOperation() {
        log.info("Attempting to undo operation");
        try {
            log.debug("Popping operation from undo stack");
            Operations operationToBeUndone = dataStore.popOperationToUndo();

            log.debug("Reverting operation");
            operationToBeUndone.revert();

            log.debug("Adding operation to redo stack");
            dataStore.addNewOperationToRedoStack(operationToBeUndone);

            log.info("Operation undone successfully");
            return new ApiResponseDTO<>(HttpStatus.OK, "Operation undone successfully", false);
        } catch (UndoStackEmptyException e) {
            log.error("Undo stack empty. Error: {}", e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.CONFLICT, e.getMessage(), true);
        } catch (Exception e) {
            log.error("Unexpected error while undoing operation. Error: {}", e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), true);
        }
    }

    @Override
    public ApiResponseDTO<?> redoOperation() {
        log.info("Attempting to redo operation");
        try {
            log.debug("Popping operation from redo stack");
            Operations operationToBeRedone = dataStore.popOperationToRedo();

            log.debug("Applying operation");
            operationToBeRedone.apply();

            log.debug("Adding operation to undo stack");
            dataStore.addNewOperationToUndoStack(operationToBeRedone);

            log.info("Operation redone successfully");
            return new ApiResponseDTO<>(HttpStatus.OK, "Operation redone successfully", false);
        } catch (RedoStackEmptyException e) {
            log.error("Redo stack empty. Error: {}", e.getMessage());
            return new ApiResponseDTO<>(HttpStatus.CONFLICT, e.getMessage(), true);
        } catch (Exception e) {
            log.error("Unexpected error while redoing operation. Error: {}", e.getMessage(), e);
            return new ApiResponseDTO<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), true);
        }
    }
}