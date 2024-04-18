package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.Exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        //TODO нужно ли обрабатывать исключение из update?
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlInsertFilm, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getTitle());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRating());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() == null) {
            //TODO выбросить нормальное исключение / подумать нужно ли вообще обрабатывать этот случай
            throw new RuntimeException("Что-то пошло не так при получении ключа созданной записи");
        } else {
            film.setId((Integer) keyHolder.getKey());
            return film;
        }
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlUpdateUser = "update \"films\" set \"title\" = ?," +
                "\"description\" = ?," +
                "\"release_date\" = ?," +
                "\"duration\" = ?" +
                "\"rating_id\" = ?" +
                "where \"id\" = ?";
        int updated = jdbcTemplate.update(sqlUpdateUser,
                film.getTitle(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating(),
                film.getId());
        if (updated == 0) {
            throw new FilmNotFoundException("Фильм с данным id не найден");
        } else {
            return film;
        }
    }

    @Override
    public List<Film> getFilms() {
        String sqlSelectUser = "select * from \"users\"";
        //TODO нужно ли обрабатывать dataAccessException
        return jdbcTemplate.query(sqlSelectUser, this::mapFilm);
    }

    @Override
    public Film getFilm(int filmId) {
        String sqlSelectUser = "select * from \"films\" where \"id\" = ?";
        //TODO нужно ли обрабатывать dataAccessException
        Film film = jdbcTemplate.queryForObject(sqlSelectUser, this::mapFilm);
        if (film == null) {
            throw new FilmNotFoundException("Фильм с таким id не найден");
        }
        return film;
    }

    public Genre getGenre(int genreId) {
        String sqlSelectGenreById = "SELECT FROM \"genres\" g \n" +
                "WHERE g.\"id\" = ?";
        Genre genre = jdbcTemplate.queryForObject(sqlSelectGenreById,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")));
        if (genre == null) {
            //TODO изменить вид исключения
            throw new FilmNotFoundException("Жанр с таким id не найден");
        }
        return genre;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sqlSelectUser = "select * from \"genres\"";
        //TODO нужно ли обрабатывать dataAccessException
        return jdbcTemplate.query(sqlSelectUser,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")));
    }



    private Film mapFilm(ResultSet rs, int rowNumber) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setTitle(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setRating(rs.getInt("rating_id"));
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

    private List<String> getFilmGenres(int filmId) {
        String sqlSelectFilmLikes = "SELECT g.\"name\" \n" +
                "FROM \"film_genres\" f \n" +
                "LEFT JOIN \"genres\" g  \n" +
                "ON f.\"genre_id\" = g.\"id\"\n" +
                "WHERE f.\"film_id\" = ?\n" +
                "ORDER BY f.\"id\" ASC ";
        return jdbcTemplate.query(sqlSelectFilmLikes,
                (rs, rowNum) -> rs.getString("name"), filmId);
    }
}
