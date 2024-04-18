package ru.yandex.practicum.filmorate.Exception;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException() {
    }

    public FriendNotFoundException(String message) {
        super(message);
    }
}
