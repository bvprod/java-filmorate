package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.Exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.Exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;

import javax.validation.ValidationException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectParameterException(final IncorrectParameterException e) {
        log.debug(String.format("Некорректное значение параметра \"%s\": %s", e.getParameter(), e.getMessage()));
        return new ErrorResponse(
                String.format(String.format("Некорректное значение параметра \"%s\": %s", e.getParameter(), e.getMessage()))
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final Throwable e) {
        log.debug(String.format("Ошибка валидации параметра: %s", e.getMessage()));
        return new ErrorResponse(String.format("Ошибка валидации параметра: %s", e.getMessage()));
    }

    @ExceptionHandler({UserNotFoundException.class, FilmNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(final RuntimeException e) {
        log.debug(String.format("Сущность с данным id не найдена: %s", e.getMessage()));
        return new ErrorResponse(String.format("Сущность с данным id не найдена: %s", e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherException(final Throwable e) {
        log.error(String.format("Произошла непредвиденная ошибка: %s", e.getMessage()));
        return new ErrorResponse(String.format("Произошла непредвиденная ошибка: %s", e.getMessage()));
    }

}
