package com.omnik.projects.task_manager.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
public class ApiResponseDTO<T> {
    private T data;
    private List<T> dataList;
    private HttpStatus status;
    private boolean isError;
    private String message;

     public ApiResponseDTO(HttpStatus status,String message,boolean isError){
        this.status= status;
        this.message=message;
        this.isError= isError;
    }

    public ApiResponseDTO(T data, List<T> dataList,HttpStatus status,String message,boolean isError){
        this.data= data;
        this.dataList=dataList;
        this.status= status;
        this.message=message;
        this.isError= isError;
    }
}
