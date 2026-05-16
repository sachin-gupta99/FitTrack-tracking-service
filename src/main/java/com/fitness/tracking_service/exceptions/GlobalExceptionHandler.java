package com.fitness.tracking_service.exceptions;

import com.fitness.tracking_service.dto.ErrorDTO;
import com.fitness.tracking_service.dto.GlobalResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<GlobalResponseDTO<ErrorDTO>> handleWebClientResponseException(WebClientResponseException exception) {
        log.error("WebClient error: {} - {}", exception.getStatusCode(), exception.getMessage());

        ErrorDTO error = ErrorDTO.of(
                "External Service Error",
                "Failed to communicate with external service: " + exception.getStatusText()
        );

        HttpStatus status = (HttpStatus) exception.getStatusCode();
        return ResponseEntity.status(status)
                .body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), status.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponseDTO<ErrorDTO>> handleException(Exception exception) {
        log.error("Exception caught: {}", exception.getMessage(), exception);

        ErrorDTO error = null;

        switch(exception) {

            case RecordNotFoundException recordNotFoundException -> {
                error = ErrorDTO.of("Not Found", recordNotFoundException.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 404));
            }
            case IllegalArgumentException illegalArgumentException -> {
                String description = (illegalArgumentException.getMessage() != null && !illegalArgumentException.getMessage().isBlank())
                        ? illegalArgumentException.getMessage()
                        : "The request is invalid";
                error = ErrorDTO.of("Bad Request", description);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 400));
            }
            case NullPointerException nullPointerException -> {
                String description = (nullPointerException.getMessage() != null && !nullPointerException.getMessage().isBlank())
                        ? nullPointerException.getMessage()
                        : "A required value was null";
                error = ErrorDTO.of("Null Value Error", description);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 500));
            }
            case RuntimeException runtimeException -> {
                String description = (runtimeException.getMessage() != null && !runtimeException.getMessage().isBlank())
                        ? runtimeException.getMessage()
                        : "An unexpected error occurred";
                error = ErrorDTO.of("Something went wrong", description);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 500));
            }
            case null, default -> {
                assert exception != null;
                String description = (exception.getMessage() != null && !exception.getMessage().isBlank())
                        ? exception.getMessage()
                        : "An internal server error occurred";
                error = ErrorDTO.of("Internal Server Error", description);
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 500));
    }
}

