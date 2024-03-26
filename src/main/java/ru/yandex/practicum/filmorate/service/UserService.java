package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int userId) {
        return userStorage.getUser(userId);
    }


    public User addFriend(int userId, int friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        user.addFriend(friend);
        friend.addFriend(user);
        log.info("Пользователь " + userId + " добавил в друзья пользователя " + friendId);
        return user;
    }

    public User removeFriend(int userId, int friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        user.removeFriend(friend);
        friend.removeFriend(user);
        log.info("Пользователь " + userId + " удалил из друзей пользователя " + friendId);
        return user;
    }

    public List<User> getUserFriends(int userId) {
        log.info("Запрошен список друзей пользователя " + userId);
        return userStorage.getUsers().stream()
                .filter(user -> getUser(userId).getFriends().contains(user.getId()))
                .collect(Collectors.toList());
    }

    public User getUser(int userId) {
        log.info("Запрошен пользователь " + userId);
        return userStorage.getUser(userId);
    }

    public List<User> getCommonFriends(int user1, int user2) {
        log.info("Запрошены общие друзья пользователей " + user1 + " и " + user2);
        List<Integer> commonUserIds = getUser(user1).getFriends().stream()
                .filter(getUser(user2).getFriends()::contains)
                .collect(Collectors.toList());
        return userStorage.getUsers().stream()
                .filter(user -> commonUserIds.contains(user.getId()))
                .collect(Collectors.toList());
    }
}
