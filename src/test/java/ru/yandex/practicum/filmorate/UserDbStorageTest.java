package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.storage.DbConstants.SQL_SELECT_USER_BY_ID;
import static ru.yandex.practicum.filmorate.storage.DbConstants.SQL_SELECT_USER_FRIENDS;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    public static User createNewTestUser() {
        User newUser = new User();
        newUser.setEmail("user@email.ru");
        newUser.setLogin("vanya123");
        newUser.setName("Ivan Petrov");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));
        return newUser;
    }

    @BeforeEach
    public void initUserStorage() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    @Order(1)
    public void testGetUsersShouldReturnEmptyList() {
        assertThat(userStorage.getUsers()).isNotNull().isEmpty();
    }

    @Test
    @Order(2)
    public void testShouldAddNewUser() {
        User newUser = userStorage.addUser(createNewTestUser());

        User savedUser = jdbcTemplate.queryForObject(SQL_SELECT_USER_BY_ID,
                this::mapUser,
                newUser.getId());

        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser);
    }

    @Test
    @Order(3)
    public void testGetUserShouldReturnUserByCorrectId() {
        User newUser = userStorage.addUser(createNewTestUser());

        User savedUser = userStorage.getUser(newUser.getId());

        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser);
    }

    @Test
    public void testGetUserShouldThrowUserNotFoundExceptionWhenInvalidUserId() {
        Assertions.assertThrows(UserNotFoundException.class, () -> userStorage.getUser(1000));
    }

    @Test
    @Order(3)
    public void testShouldCorrectlyUpdateUserById() {
        User user = userStorage.addUser(createNewTestUser());
        user.setName("Другое имя");
        user.setEmail("otherEmail@mail.ru");
        User updatedUser = userStorage.updateUser(user);

        assertThat(updatedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    @Order(4)
    public void testShouldThrowUserNotFoundExceptionWhenUpdatingUserWithInvalidId() {
        User user = userStorage.addUser(createNewTestUser());
        user.setName("Другое имя");
        user.setEmail("otherEmail@mail.ru");
        user.setId(1000);
        Assertions.assertThrows(UserNotFoundException.class, () -> userStorage.updateUser(user));
    }

    @Test
    @Order(5)
    public void testShouldReturnAllUsers() {
        User user1 = userStorage.addUser(createNewTestUser());
        User user2 = userStorage.addUser(createNewTestUser());
        User user3 = userStorage.addUser(createNewTestUser());

        List<User> users = userStorage.getUsers();

        assertThat(users).isNotNull().containsAll(List.of(user1, user2, user3));
    }

    @Test
    @Order(6)
    public void testShouldAddFriend() {
        User user1 = userStorage.addUser(createNewTestUser());
        User user2 = userStorage.addUser(createNewTestUser());

        userStorage.addFriend(user1.getId(), user2.getId());
        UserService userService = new UserService(userStorage);
        assertThat(userService.getUserFriends(user1.getId()))
                .isNotNull()
                .contains(userStorage.getUser(user2.getId()));
        assertThat(userService.getUserFriends(user2.getId()))
                .isNotNull()
                .isEmpty();
    }

    @Test
    @Order(7)
    public void testShouldThrowDataIntegrityViolationExceptionWhenAddingFriendWithInvalidId() {
        User user1 = userStorage.addUser(createNewTestUser());

        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userStorage.addFriend(user1.getId(), 60));
        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userStorage.addFriend(60, user1.getId()));
    }

    @Test
    @Order(8)
    public void testShouldRemoveFriend() {
        User user1 = userStorage.addUser(createNewTestUser());
        User user2 = userStorage.addUser(createNewTestUser());
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());
        UserService userService = new UserService(userStorage);
        assertThat(userService.getUserFriends(user1.getId()))
                .isNotNull()
                .isEmpty();
        assertThat(userService.getUserFriends(user2.getId()))
                .isNotNull()
                .isEmpty();
    }

    @Test
    @Order(9)
    public void testShouldApproveFriend() {
        User user1 = userStorage.addUser(createNewTestUser());
        User user2 = userStorage.addUser(createNewTestUser());

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.approveFriend(user1.getId(), user2.getId());
        UserService userService = new UserService(userStorage);
        assertThat(userService.getUserFriends(user1.getId()))
                .isNotNull()
                .contains(userStorage.getUser(user2.getId()));
        assertThat(userService.getUserFriends(user2.getId()))
                .isNotNull()
                .contains(userStorage.getUser(user1.getId()));
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