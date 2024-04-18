package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.Exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.Exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.Exception.RatingNotFoundException;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.security.GeneralSecurityException;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        //TODO жанры добавить???
        String sqlInsertFilm = "INSERT INTO \"films\" (\"title\", \"description\", \"release_date\", \"duration\", \"rating_id\") " +
                "VALUES(?, ?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlInsertFilm, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            //TODO выбросить нормальное исключение / подумать нужно ли вообще обрабатывать этот случай
            throw new RuntimeException("Что-то пошло не так при получении ключа созданной записи");
        } else {
            film.setId((Integer) keyHolder.getKey());
            addFilmGenres(film);
            return getFilm(film.getId());
        }
    }

    private void addFilmGenres(Film film) {
        String sqlInsertFilm = "INSERT INTO \"film_genres\" (\"film_id\", \"genre_id\") VALUES (?, ?)";
        List<Genre> distinctGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
        for (Genre genre: distinctGenres) {
            jdbcTemplate.update(sqlInsertFilm, film.getId(), genre.getId());
        }
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlUpdateUser = "update \"films\" set \"title\" = ?, " +
                "\"description\" = ?, " +
                "\"release_date\" = ?, " +
                "\"duration\" = ? ," +
                "\"rating_id\" = ? " +
                "where \"id\" = ?";
        jdbcTemplate.update(sqlUpdateUser,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        updateFilmGenres(film);
        return getFilm(film.getId());
    }

    private void updateFilmGenres(Film film) {
        if (film.getGenres().isEmpty()) {
            return;
        } else {
            deleteFilmGenres(film.getId());
        }
        String sqlInsertFilmGenres = "INSERT INTO \"film_genres\" (\"film_id\", \"genre_id\") VALUES (?, ?)";
        List<Genre> distinctGenres = film.getGenres().stream().distinct().collect(Collectors.toList());
        for (Genre genre: distinctGenres) {
            jdbcTemplate.update(sqlInsertFilmGenres, film.getId(), genre.getId());
        }
    }

    private void deleteFilmGenres(int filmId) {
        String sqlDeleteFilmGenres = "DELETE FROM \"film_genres\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlDeleteFilmGenres, filmId);
    }

    @Override
    public List<Film> getFilms() {
        String sqlSelectUser = "select * from \"films\"";
        //TODO нужно ли обрабатывать dataAccessException
        return jdbcTemplate.query(sqlSelectUser, this::mapFilm);
    }

    @Override
    public Film getFilm(int filmId) {
        String sqlSelectUser = "select * from \"films\" where \"id\" = ?";
        //TODO нужно ли обрабатывать dataAccessException
        try {
            return jdbcTemplate.queryForObject(sqlSelectUser, this::mapFilm, filmId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new FilmNotFoundException("Фильм с таким id не найден");
        }
    }

    public void deleteFilm(int filmId) {
        String sqlSelectUser = "delete * from \"films\" where \"id\" = ?";
        //TODO нужно ли обрабатывать dataAccessException
        deleteFilmGenres(filmId);
        jdbcTemplate.update(sqlSelectUser, filmId);
    }

    public Genre getGenre(int genreId) {
        String sqlSelectGenreById = "SELECT * FROM \"genres\" g \n" +
                "WHERE g.\"id\" = ?";
        try {
            return jdbcTemplate.queryForObject(sqlSelectGenreById,
                    (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), genreId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new GenreNotFoundException("Жанр с таким id не найден");
        }
    }

    public Mpa getRating(int ratingId) {
        //TODO Убрать звездочку и прописать явно поля
        String sqlSelectRatingById = "SELECT * FROM \"ratings\" WHERE \"id\" = ?";
        try {
            return jdbcTemplate.queryForObject(sqlSelectRatingById,
                    (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("rating_title")),
                    ratingId);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new RatingNotFoundException("Рейтинг с таким id не найден");
        }
    }

    @Override
    public List<Mpa> getAllRatings() {
        String sqlSelectUser = "select * from \"ratings\"";
        //TODO нужно ли обрабатывать dataAccessException
        return jdbcTemplate.query(sqlSelectUser,
                (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("rating_title")));
    }

    @Override
    public List<Genre> getAllGenres() {
        String sqlSelectUser = "select * from \"genres\"";
        //TODO нужно ли обрабатывать dataAccessException
        return jdbcTemplate.query(sqlSelectUser,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Film addLike(int filmId, int userId) {
        String sqlInsertLike = "INSERT INTO \"likes\" (\"film_id\", \"user_id\", \"created_at\") " +
                "VALUES (?, ?, CURRENT_TIMESTAMP())\n";
        jdbcTemplate.update(sqlInsertLike, filmId, userId);
        return getFilm(filmId);
    }

    @Override
    public Film removeLike(int filmId, int userId) {
        String sqlDeleteLike = "DELETE FROM \"likes\" WHERE \"film_id\" = ? AND \"user_id\" = ?";
        jdbcTemplate.update(sqlDeleteLike, filmId, userId);
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
        String sqlSelectFilmLikes = "select \"user_id\" " +
                "from \"likes\" " +
                "where \"film_id\" = ?";
        List<Integer> likes = jdbcTemplate.query(sqlSelectFilmLikes,
                (rs, rowNum) -> rs.getInt("user_id"), filmId);
        return new HashSet<>(likes);
    }

    private List<Genre> getFilmGenres(int filmId) {
        String sqlSelectFilmLikes = "SELECT g.\"id\", g.\"name\" \n" +
                "FROM \"film_genres\" f \n" +
                "LEFT JOIN \"genres\" g  \n" +
                "ON f.\"genre_id\" = g.\"id\"\n" +
                "WHERE f.\"film_id\" = ?\n" +
                "ORDER BY f.\"id\" ASC ";
        return jdbcTemplate.query(sqlSelectFilmLikes,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }
}
