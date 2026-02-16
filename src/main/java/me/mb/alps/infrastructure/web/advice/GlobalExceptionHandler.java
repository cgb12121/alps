package me.mb.alps.infrastructure.web.advice;

import me.mb.alps.application.exception.AlpsException;
import me.mb.alps.application.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps application and validation exceptions to HTTP responses (RFC 7807 ProblemDetail).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setTitle("Not Found");
        if (ex.getResource() != null) {
            detail.setProperty("resource", ex.getResource());
        }
        if (ex.getId() != null) {
            detail.setProperty("id", ex.getId().toString());
        }
        return detail;
    }

    @ExceptionHandler(AlpsException.class)
    public ProblemDetail handleAlps(AlpsException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        detail.setTitle("Business Error");
        if (ex.getErrorCode() != null) {
            detail.setProperty("errorCode", ex.getErrorCode());
        }
        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setTitle("Validation Error");
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        detail.setProperty("errors", errors);
        return detail;
    }
}
