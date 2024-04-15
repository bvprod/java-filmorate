package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    @Override
    public Film addFilm(Film film) {
        return null;
    }

    @Override
    public Film updateFilm(Film film) {
        return null;
    }

    @Override
    public List<Film> getFilms() {
        return null;
    }

    @Override
    public Film getFilm(int filmId) {
        return null;
    }
}
