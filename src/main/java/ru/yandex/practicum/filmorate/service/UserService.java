package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public boolean addFriend(User user1, User user2) {
        return user1.addFriend(user2) && user2.addFriend(user1);
    }

    public boolean removeFriend(User user1, User user2) {
        return user1.removeFriend(user2) && user2.removeFriend(user1);
    }

    public Set<User> getUserFriends(User user) {
        return user.getFriends();
    }

    public List<User> getMutualFriends(User user1, User user2) {
        return user1.getFriends().stream().filter(user2.getFriends()::contains).collect(Collectors.toList());
    }
}
