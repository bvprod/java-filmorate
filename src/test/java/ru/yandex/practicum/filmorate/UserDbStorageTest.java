package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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
class UserDbStorageTest {
    private static int userKeyCounter = 0;
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
        User newUser = createNewTestUser();
        int userKey = ++userKeyCounter;
        newUser.setId(userKey);
        userStorage.addUser(newUser);

        User savedUser = jdbcTemplate.queryForObject(SQL_SELECT_USER_BY_ID,
                this::mapUser,
                userKey);

        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser);
    }

    @Test
    @Order(3)
    public void testGetUserShouldReturnUserByCorrectId() {
        User newUser = createNewTestUser();
        int userKey = ++userKeyCounter;
        newUser.setId(userKey);
        userStorage.addUser(newUser);

        User savedUser = userStorage.getUser(userKey);

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
        User user = createNewTestUser();
        int userKey = ++userKeyCounter;
        user.setId(userKey);
        userStorage.addUser(user);
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
        User user = createNewTestUser();
        user.setId(++userKeyCounter);
        userStorage.addUser(user);
        user.setName("Другое имя");
        user.setEmail("otherEmail@mail.ru");
        user.setId(1000);
        Assertions.assertThrows(UserNotFoundException.class, () -> userStorage.updateUser(user));
    }

    @Test
    @Order(5)
    public void testShouldReturnAllUsers() {
        User user1 = createNewTestUser();
        user1.setId(++userKeyCounter);
        User user2 = createNewTestUser();
        user2.setId(++userKeyCounter);
        User user3 = createNewTestUser();
        user3.setId(++userKeyCounter);
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);

        List<User> users = userStorage.getUsers();

        assertThat(users).isNotNull().containsAll(List.of(user1, user2, user3));
    }

    @Test
    @Order(6)
    public void testShouldAddFriend() {
        User user1 = createNewTestUser();
        User user2 = createNewTestUser();
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        int user1Key = ++userKeyCounter;
        int user2Key = ++userKeyCounter;
        userStorage.addFriend(user1Key, user2Key);
        UserService userService = new UserService(userStorage);
        assertThat(userService.getUserFriends(user1Key))
                .isNotNull()
                .contains(userStorage.getUser(user2Key));
        assertThat(userService.getUserFriends(user2Key))
                .isNotNull()
                .isEmpty();
    }

    @Test
    @Order(7)
    public void testShouldThrowDataIntegrityViolationExceptionWhenAddingFriendWithInvalidId() {
        User user1 = createNewTestUser();
        userStorage.addUser(user1);
        int userKey = ++userKeyCounter;
        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userStorage.addFriend(userKey, 60));
        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userStorage.addFriend(60, userKey));
    }

    @Test
    @Order(8)
    public void testShouldRemoveFriend() {
        User user1 = createNewTestUser();
        User user2 = createNewTestUser();
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        int user1Key = ++userKeyCounter;
        int user2Key = ++userKeyCounter;
        userStorage.addFriend(user1Key, user2Key);
        userStorage.removeFriend(user1Key, user2Key);
        UserService userService = new UserService(userStorage);
        assertThat(userService.getUserFriends(user1Key))
                .isNotNull()
                .isEmpty();
        assertThat(userService.getUserFriends(user2Key))
                .isNotNull()
                .isEmpty();
    }

    @Test
    @Order(9)
    public void testShouldApproveFriend() {
        User user1 = createNewTestUser();
        User user2 = createNewTestUser();
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        int user1Key = ++userKeyCounter;
        int user2Key = ++userKeyCounter;
        userStorage.addFriend(user1Key, user2Key);
        userStorage.approveFriend(user1Key, user2Key);
        UserService userService = new UserService(userStorage);
        assertThat(userService.getUserFriends(user1Key))
                .isNotNull()
                .contains(userStorage.getUser(user2Key));
        assertThat(userService.getUserFriends(user2Key))
                .isNotNull()
                .contains(userStorage.getUser(user1Key));
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