package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(int userId, int friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        user.addFriend(friend);
        friend.addFriend(user);
        return user;
    }

    public User removeFriend(int userId, int friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        user.removeFriend(friend);
        friend.removeFriend(user);
        return user;
    }

    public Set<User> getUserFriends(int userId) {
        return getUser(userId).getFriends();
    }

    public User getUser(int userId) {
        return userStorage.getUser(userId);
    }

    public List<User> getCommonFriends(int user1, int user2) {
        return getUser(user1).getFriends().stream()
                .filter(getUser(user2).getFriends()::contains)
                .collect(Collectors.toList());
    }
}
