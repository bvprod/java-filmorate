package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUser(int userId);

    User addFriend(int userId, int friendId);

    User removeFriend(int userId, int friendId);

    void approveFriend(int requestFrom, int requestTo);

    List<User> getUserFriends(int userId);
}
