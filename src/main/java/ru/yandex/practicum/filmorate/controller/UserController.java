package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final HashSet<User> users = new HashSet<>();
    private int idCounter = 1;

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        user.setId(idCounter++);
        users.add(user);
        log.info("Пользователь " + user.getId() + " добавлен");
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (users.contains(user)) {
            users.remove(user);
            users.add(user);
            log.info("Информация о пользователе " + user.getId() + " обновлена");
            return user;
        } else {
            log.warn("Пользователь с id " + user.getId() + " не найден");
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }
    }

    @GetMapping
    public Set<User> getUsers() {
        return users;
    }
}
