package shareit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shareit.handler.exception.NotFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFoundHandler(NotFoundException e) {
        log.warn(e.getMessage());
        return Map.of(
                ERROR, "Объект не найден.",
                MESSAGE, e.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public java.util.Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String errorMessage = fieldError.map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("Validation error");
        log.warn("Validation failed: {}", errorMessage);
        return java.util.Map.of(
                ERROR, "Ошибка валидации",
                MESSAGE, errorMessage
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public java.util.Map<String, String> handleIllegalArgs(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return java.util.Map.of(
                ERROR, "Неверный аргумент",
                MESSAGE, ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public java.util.Map<String, String> handleAll(Exception ex) {
        log.error("Unexpected error: ", ex);
        return java.util.Map.of(
                ERROR, "Внутренняя ошибка сервера",
                MESSAGE, ex.getMessage()
        );
    }

    @ExceptionHandler(jakarta.validation.ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(jakarta.validation.ValidationException ex) {
        return Map.of(
                ERROR, "Ошибка валидации",
                MESSAGE, ex.getMessage()
        );
    }
}