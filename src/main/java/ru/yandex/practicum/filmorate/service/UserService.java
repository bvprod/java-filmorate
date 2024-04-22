package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Exception.FriendNotFoundException;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        log.info("Запрос на добавление пользователя " + user.getLogin());
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        log.info("Запрос на обновление пользователя " + user.getLogin());
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        log.info("Запрос на получение списка всех пользователей");
        return userStorage.getUsers();
    }

    public User getUser(int userId) {
        log.info("Запрошен пользователь с id = " + userId);
        return userStorage.getUser(userId);
    }

    public User addFriend(int userId, int friendId) {
        log.info("Запрос на добавление в друзья от " + userId + " к " + friendId);
        userStorage.getUser(userId);
        userStorage.getUser(friendId);
        return userStorage.addFriend(userId, friendId);
    }

    public User removeFriend(int userId, int friendId) {
        log.info("Запрос на удаление из друзей от " + userId + " в отношении " + friendId);
        userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        if (!getUserFriends(userId).contains(friend)) {
            throw new FriendNotFoundException("Друг с таким id не найден");
        }
        return userStorage.removeFriend(userId, friendId);
    }

    public List<User> getUserFriends(int userId) {
        log.info("Запрошен список друзей пользователя с id = " + userId);
        userStorage.getUser(userId);
        return userStorage.getUserFriends(userId);
    }

    public void approveFriend(int requestFrom, int requestTo) {
        log.info("Запрос на одобрение запроса о дружбе от " + requestFrom + " к " + requestTo);
        User user = userStorage.getUser(requestFrom);
        if (user.getFriends().contains(requestTo)) {
            userStorage.approveFriend(requestFrom, requestTo);
        } else {
            throw new UserNotFoundException("Id не был найден в списке друзей");
        }
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
