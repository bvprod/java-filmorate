package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(int filmId);

    Genre getGenre(int genreId);

    List<Genre> getAllGenres();

    Film addLike(int filmId, int userId);

    Film removeLike(int filmId, int userId);
}
