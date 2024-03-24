package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @Override
    public User addUser(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Пользователь " + user.getId() + " добавлен");
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Информация о пользователе " + user.getId() + " обновлена");
            return user;
        }
        log.info("Пользователь с id " + user.getId() + " не найден");
        log.warn("Пользователь с id " + user.getId() + " не найден");
        throw new UserNotFoundException("Пользователь с id " + user.getId() + " не найден");
    }

    @Override
    public List<User> getUsers() {
        log.warn("Запрошен список всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(int userId) {
        log.warn("Запрошен пользователь с id " + userId);
        if (!users.containsKey(userId)) {
            log.info("Пользователь с id " + userId + " не найден");
            log.warn("Пользователь с id " + userId + " не найден");
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }
        return users.get(userId);
    }

}
