package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.handler.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private final String error = "error";
    private final String message = "message";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFoundHandler(NotFoundException e) {
        log.warn(e.getMessage());
        return Map.of(
                error, "Объект не найден.",
                message, e.getMessage()
        );
    }
}