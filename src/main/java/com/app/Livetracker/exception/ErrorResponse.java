package com.app.Livetracker.exception;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private int status;              // HTTP status code
    private String error;             // Error type
    private String message;           // Human readable message
    private String path;              // API path
    private LocalDateTime timestamp;  // Time of error
}

