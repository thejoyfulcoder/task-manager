package com.omnik.projects.task_manager.service;

import com.omnik.projects.task_manager.dto.response.ApiResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface UndoRedoOperationsService {

    ApiResponseDTO<?> undoOperation();

    ApiResponseDTO<?> redoOperation();
}
