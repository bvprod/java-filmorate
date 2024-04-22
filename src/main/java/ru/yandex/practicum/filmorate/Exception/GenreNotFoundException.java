package ru.yandex.practicum.filmorate.Exception;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException() {
    }

    public GenreNotFoundException(String message) {
        super(message);
    }
}
