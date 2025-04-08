package com.omnik.projects.task_manager.controller;

import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/operations")
public interface UndoRedoOperationsController {

    @PostMapping("/undo")
    ResponseEntity<ApiResponseDTO<?>> undoOperation();

    @PostMapping("/redo")
    ResponseEntity<ApiResponseDTO<?>> redoOperation();
}
