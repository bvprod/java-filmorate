package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        String sqlInsertUser = "INSERT INTO \"users\" (\"email\", \"login\", \"name\", \"birthday\") " +
                "VALUES(?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        //TODO нужно ли обрабатывать исключение из update?
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlInsertUser, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            //TODO выбросить нормальное исключение / подумать нужно ли вообще обрабатывать этот случай
            throw new RuntimeException("Что-то пошло не так при получении ключа созданной записи");
        } else {
            user.setId((Integer) keyHolder.getKey());
            return user;
        }
    }

    @Override
    public User updateUser(User user) {
        String sqlUpdateUser = "update \"users\" set \"email\" = ?," +
                "\"login\" = ?," +
                "\"name\" = ?," +
                "\"birthday\" = ?" +
                "where \"id\" = ?";
        int updated = jdbcTemplate.update(sqlUpdateUser,
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
        String sqlSelectUser = "select * from \"users\"";
        //TODO нужно ли обрабатывать dataAccessException
        return jdbcTemplate.query(sqlSelectUser, this::mapUser);
    }

    @Override
    public User getUser(int userId) {
        String sqlSelectUser = "select * from \"users\" where \"id\" = ?";
        //TODO нужно ли обрабатывать dataAccessException
        User user = jdbcTemplate.queryForObject(sqlSelectUser, this::mapUser, userId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с таким id не найден");
        }
        return user;
    }

    @Override
    public User addFriend(int userId, int friendId) {
        String sqlInsertFriend = "INSERT INTO \"friends\" (\"user_id\", \"friend_id\", \"request_status\", \"created_at\")" +
                " VALUES (?, ?, false, CURRENT_TIMESTAMP());";
        int rowsInserted = jdbcTemplate.update(sqlInsertFriend,
                userId, friendId);

        if (rowsInserted != 1) {
            throw new RuntimeException("Что-то пошло не так при добавлении в друзья");
        }
        return getUser(userId);
    }

    @Override
    public User removeFriend(int userId, int friendId) {
        String sqlDeleteFriend = "DELETE FROM \"friends\" WHERE \"user_id\" = ? AND \"friend_id\" = ?";
        int rowsDeleted = jdbcTemplate.update(sqlDeleteFriend,
                userId, friendId);
        if (rowsDeleted != 1) {
            throw new RuntimeException("Что-то пошло не так при удалении из друзей");
        }
        return getUser(userId);
    }

    @Override
    public void approveFriend(int requestFrom, int requestTo) {
        String sqlUpdateFriendRequestStatus = "UPDATE \"friends\" SET \"request_status\" = TRUE " +
                "WHERE \"user_id\" = ? AND \"friend_id\" = ?";
        int rowUpdated = jdbcTemplate.update(sqlUpdateFriendRequestStatus, requestFrom, requestTo);
        if (rowUpdated != 1) {
            throw new RuntimeException("Что-то пошло не так при добавлении в друзья");
        }
    }

    private User mapUser(ResultSet rs, int rowNumber) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setLogin(rs.getString("login"));
        user.setEmail(rs.getString("email"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(getUserFriends(user.getId()));
        return user;
    }

    private Set<Integer> getUserFriends(int userId) {
        String sqlSelectUserFriends = "SELECT DISTINCT " +
                "CASE WHEN \"user_id\" = ? THEN \"friend_id\" ELSE \"user_id\" END AS \"friends_ids\"\n" +
                "FROM \"friends\" \n" +
                "WHERE \"user_id\" = ? OR (\"friend_id\" = ? AND \"request_status\" = true)";
        List<Integer> friends_ids = jdbcTemplate.query(sqlSelectUserFriends,
                (rs, rowNum) -> rs.getInt("friends_ids"), userId, userId, userId);
        return new HashSet<>(friends_ids);
    }
}
