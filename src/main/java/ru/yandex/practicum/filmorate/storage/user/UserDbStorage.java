package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.yandex.practicum.filmorate.storage.DbConstants.*;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new RuntimeException("Что-то пошло не так при получении ключа созданной записи");
        } else {
            user.setId((Integer) keyHolder.getKey());
            return user;
        }
    }

    @Override
    public User updateUser(User user) {
        int updated = jdbcTemplate.update(SQL_UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        if (updated == 0) {
            throw new UserNotFoundException("Пользователь с данным id не найден");
        } else {
            return user;
        }
    }

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query(SQL_SELECT_ALL_USERS, this::mapUser);
    }

    @Override
    public User getUser(int userId) {
        try {
            return jdbcTemplate.queryForObject(SQL_SELECT_USER_BY_ID, this::mapUser, userId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new UserNotFoundException("Пользователь с таким id не найден");
        }
    }

    @Override
    public User addFriend(int userId, int friendId) {
        jdbcTemplate.update(SQL_INSERT_FRIEND, userId, friendId);
        return getUser(userId);
    }

    @Override
    public User removeFriend(int userId, int friendId) {
        jdbcTemplate.update(SQL_DELETE_FRIEND, userId, friendId);
        return getUser(userId);
    }

    @Override
    public void approveFriend(int requestFrom, int requestTo) {
        jdbcTemplate.update(SQL_APPROVE_FRIEND, requestFrom, requestTo);
    }

    private User mapUser(ResultSet rs, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setLogin(rs.getString("login"));
        user.setEmail(rs.getString("email"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(getUserFriends(user.getId()));
        return user;
    }

    private Set<Integer> getUserFriends(int userId) {
        List<Integer> friendsIds = jdbcTemplate.query(SQL_SELECT_USER_FRIENDS,
                (rs, rowNum) -> rs.getInt("friends_ids"), userId, userId, userId);
        return new HashSet<>(friendsIds);
    }
}
