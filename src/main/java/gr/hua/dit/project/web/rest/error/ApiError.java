package gr.hua.dit.project.web.rest.error;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ApiError(Integer status, String message, String path, LocalDateTime timestamp){

    public ApiError(HttpStatus status, String message, String path) {
        this(status.value(), message,path,LocalDateTime.now());
    }

}
