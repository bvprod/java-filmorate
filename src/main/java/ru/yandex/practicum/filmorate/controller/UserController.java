package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable("userId") Integer userId) {
        return userService.getUser(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id") Integer userId, @PathVariable("friendId") Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IncorrectParameterException("Идентификаторы не могут быть одинаковые");
        }
        return userService.addFriend(userId, friendId);
    }

    @PatchMapping("/{requestFromId}/friends/{requestToId}")
    public Map<String, String> approveFriend(@PathVariable("requestFromId") Integer requestFrom,
                                             @PathVariable("requestToId") Integer requestTo) {
        if (requestFrom.equals(requestTo)) {
            throw new IncorrectParameterException("Идентификаторы не могут быть одинаковые");
        }
        userService.approveFriend(requestFrom, requestTo);
        return Map.of("status", "Friendship approved!");
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable("id") Integer userId, @PathVariable("friendId") Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IncorrectParameterException("Идентификаторы не могут быть одинаковые");
        }
        return userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable("id") Integer userId) {
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") Integer userId, @PathVariable("otherId") Integer otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new IncorrectParameterException("Идентификаторы не могут быть одинаковые");
        }
        return userService.getCommonFriends(userId, otherUserId);
    }
}
