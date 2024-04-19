package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.Exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.Exception.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.storage.DbConstants.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;

    @BeforeEach
    public void initUserStorage() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    @Order(1)
    public void testGetFilmsShouldReturnEmptyList() {
        assertThat(filmStorage.getFilms()).isNotNull().isEmpty();
    }

    @Test
    @Order(2)
    public void testShouldAddNewFilm() {
        Film film = filmStorage.addFilm(createNewTestFilm());

        Film savedFilm = jdbcTemplate.queryForObject(SQL_SELECT_FILM_BY_ID,
                this::mapFilm,
                film.getId());

        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(film);
    }

    @Test
    @Order(3)
    public void testGetFilmShouldReturnFilmByCorrectId() {
        Film film = filmStorage.addFilm(createNewTestFilm());

        Film savedFilm = filmStorage.getFilm(film.getId());

        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(film);
    }

    @Test
    public void testGetFilmShouldThrowFilmNotFoundWithInvalidId() {
        Assertions.assertThrows(FilmNotFoundException.class, () -> filmStorage.getFilm(1000));
    }

    @Test
    @Order(3)
    public void testShouldCorrectlyUpdateUserById() {
        Film film = filmStorage.addFilm(createNewTestFilm());
        film.setName("Другое имя");
        film.setDescription("otherEmail@mail.ru");
        Film updatedFilm = filmStorage.updateFilm(film);

        assertThat(updatedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(film);
    }

    @Test
    @Order(4)
    public void testShouldThrowDataIntegrityViolationException() {
        Film film = filmStorage.addFilm(createNewTestFilm());
        film.setName("Другое имя");
        film.setDescription("другой фильм");
        film.setId(1000);
        Assertions.assertDoesNotThrow(() -> filmStorage.updateFilm(film));
        Assertions.assertThrows(FilmNotFoundException.class, () -> filmStorage.getFilm(1000));
    }

    @Test
    @Order(5)
    public void testShouldReturnAllFilms() {
        Film film = filmStorage.addFilm(createNewTestFilm());
        Film film2 = filmStorage.addFilm(createNewTestFilm());
        Film film3 = filmStorage.addFilm(createNewTestFilm());

        List<Film> films = filmStorage.getFilms();

        assertThat(films).isNotNull().containsAll(List.of(film, film2, film3));
    }

    @Test
    @Order(6)
    public void testShouldAddLike() {
        Film film = filmStorage.addFilm(createNewTestFilm());
        User user = UserDbStorageTest.createNewTestUser();
        user = userStorage.addUser(user);
        Film updated = filmStorage.addLike(film.getId(), user.getId());
        assertThat(updated).isNotNull();
        Assertions.assertEquals(updated.getLikes(), Set.of(user.getId()));
    }

    @Test
    @Order(7)
    public void testShouldRemoveLike() {
        Film film = filmStorage.addFilm(createNewTestFilm());
        User user = UserDbStorageTest.createNewTestUser();
        user = userStorage.addUser(user);
        filmStorage.addLike(film.getId(), user.getId());
        Film updated = filmStorage.removeLike(film.getId(), user.getId());
        assertThat(updated).isNotNull();
        Assertions.assertEquals(updated.getLikes(), Collections.EMPTY_SET);
    }

    @Test
    public void testShouldReturnAllGenres() {
        assertThat(filmStorage.getAllGenres()).isNotNull().containsAll(List.of(new Genre(1, "Комедия"),
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм"),
                new Genre(4, "Триллер"),
                new Genre(5, "Документальный"),
                new Genre(6, "Боевик")));
    }

    @Test
    public void testShouldReturnGenreById() {
        assertThat(filmStorage.getGenre(1)).isNotNull().isEqualTo(new Genre(1, "Драма"));
    }

    @Test
    public void testShouldReturnAllMpa() {
        assertThat(filmStorage.getAllRatings()).isNotNull().containsAll(List.of(new Mpa(1, "G"),
                new Mpa(2, "PG"),
                new Mpa(3, "PG-13"),
                new Mpa(4, "R"),
                new Mpa(5, "NC-17")));
    }

    @Test
    public void testShouldReturnMpaById() {
        assertThat(filmStorage.getRating(1)).isNotNull().isEqualTo(new Mpa(1, "G"));
    }


    private Film createNewTestFilm() {
        Film newFilm = new Film();
        newFilm.setName("test film");
        newFilm.setDuration(100);
        newFilm.setDescription("blablabla");
        newFilm.setReleaseDate(LocalDate.of(2000, 2, 2));
        newFilm.setMpa(new Mpa(1, "G"));
        newFilm.setGenres(List.of(new Genre(1, "Комедия")));
        return newFilm;
    }

    private Film mapFilm(ResultSet rs, int rowNumber) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(getRating(rs.getInt("rating_id")));
        film.setLikes(getFilmLikes(film.getId()));
        film.setGenres(getFilmGenres(film.getId()));
        return film;
    }

    private Set<Integer> getFilmLikes(int filmId) {
        List<Integer> likes = jdbcTemplate.query(SQL_SELECT_LIKES_BY_FILM_ID,
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId);
        return new HashSet<>(likes);
    }

    private List<Genre> getFilmGenres(int filmId) {
        return jdbcTemplate.query(SQL_SELECT_FILM_GENRES_BY_FILM_ID, (rs, rowNum) -> new Genre(rs.getInt("id"),
                rs.getString("name")), filmId);
    }

    private Mpa getRating(int ratingId) {
        try {
            return jdbcTemplate.queryForObject(SQL_SELECT_RATING_BY_ID,
                    (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("rating_title")),
                    ratingId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new RatingNotFoundException("Рейтинг с таким id не найден");
        }
    }

}
