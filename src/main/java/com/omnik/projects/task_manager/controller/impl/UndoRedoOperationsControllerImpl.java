package com.omnik.projects.task_manager.controller.impl;

import com.omnik.projects.task_manager.controller.UndoRedoOperationsController;
import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import com.omnik.projects.task_manager.service.UndoRedoOperationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UndoRedoOperationsControllerImpl implements UndoRedoOperationsController {

    private final UndoRedoOperationsService undoRedoOperationsService;

    public UndoRedoOperationsControllerImpl(UndoRedoOperationsService undoRedoOperationsService) {
        this.undoRedoOperationsService = undoRedoOperationsService;
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> undoOperation() {
        return ResponseEntity.ok(undoRedoOperationsService.undoOperation());
    }

    @Override
    public ResponseEntity<ApiResponseDTO<?>> redoOperation() {
        return ResponseEntity.ok(undoRedoOperationsService.redoOperation());
    }
}
