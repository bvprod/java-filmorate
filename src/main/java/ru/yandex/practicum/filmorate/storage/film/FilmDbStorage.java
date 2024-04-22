package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.Exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.Exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.Exception.RatingNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.DbConstants.*;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            throw new RuntimeException("Что-то пошло не так при получении ключа созданной записи");
        } else {
            film.setId((Integer) keyHolder.getKey());
            addFilmGenres(film);
            return getFilm(film.getId());
        }
    }

    private void addFilmGenres(Film film) {
        List<Genre> distinctGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
        for (Genre genre : distinctGenres) {
            jdbcTemplate.update(SQL_INSERT_FILM_GENRES, film.getId(), genre.getId());
        }
    }

    @Override
    public Film updateFilm(Film film) {
        int rowCount = jdbcTemplate.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        if (rowCount == 0) {
            return film;
        }
        updateFilmGenres(film);
        return getFilm(film.getId());
    }

    @Override
    public List<Film> getFilms() {
        return jdbcTemplate.query(SQL_SELECT_ALL_FILMS, this::mapFilm);
    }

    @Override
    public Film getFilm(int filmId) {
        try {
            return jdbcTemplate.queryForObject(SQL_SELECT_FILM_BY_ID, this::mapFilm, filmId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new FilmNotFoundException("Фильм с таким id не найден");
        }
    }

    @Override
    public void deleteFilm(int filmId) {
        deleteFilmGenres(filmId);
        jdbcTemplate.update(SQL_DELETE_FILM_BY_ID, filmId);
    }

    @Override
    public Genre getGenre(int genreId) {
        try {
            return jdbcTemplate.queryForObject(SQL_SELECT_GENRE_BY_ID,
                    (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                    genreId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new GenreNotFoundException("Жанр с таким id не найден");
        }
    }

    @Override
    public Mpa getRating(int ratingId) {
        try {
            return jdbcTemplate.queryForObject(SQL_SELECT_RATING_BY_ID,
                    (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("rating_title")),
                    ratingId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new RatingNotFoundException("Рейтинг с таким id не найден");
        }
    }

    @Override
    public List<Mpa> getAllRatings() {
        return jdbcTemplate.query(SQL_SELECT_ALL_RATINGS, (rs, rowNum) -> new Mpa(rs.getInt("id"),
                rs.getString("rating_title")));
    }

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query(SQL_SELECT_ALL_GENRES, (rs, rowNum) -> new Genre(rs.getInt("id"),
                rs.getString("name")));
    }

    @Override
    public Film addLike(int filmId, int userId) {
        jdbcTemplate.update(SQL_INSERT_LIKE, filmId, userId);
        return getFilm(filmId);
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        jdbcTemplate.update(SQL_DELETE_LIKE, filmId, userId);
        return getFilm(filmId);
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

    private void updateFilmGenres(Film film) {
        if (film.getGenres().isEmpty()) {
            return;
        } else {
            deleteFilmGenres(film.getId());
        }
        List<Genre> distinctGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
        for (Genre genre : distinctGenres) {
            jdbcTemplate.update(SQL_INSERT_FILM_GENRES, film.getId(), genre.getId());
        }
    }

    private void deleteFilmGenres(int filmId) {
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES_BY_FILM_ID, filmId);
    }
}
