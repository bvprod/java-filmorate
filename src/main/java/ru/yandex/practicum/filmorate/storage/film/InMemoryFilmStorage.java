package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.Exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.yandex.practicum.filmorate.storage.Constants.EARLIEST_FILM_RELEASE_DATE;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    private int idCounter = 1;

    @Override
    public Film addFilm(Film film) {
        checkFilmReleaseDate(film.getReleaseDate());
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Фильм " + film.getId() + " добавлен. Всего фильмов в коллекции: " + films.size());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        checkFilmReleaseDate(film.getReleaseDate());
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм " + film.getId() + " обновлен");
            return film;
        }
        log.info("Фильм с id " + film.getId() + " не найден");
        log.warn("Фильм с id " + film.getId() + " не найден");
        throw new FilmNotFoundException("Фильм с id " + film.getId() + " не найден");
    }

    @Override
    public List<Film> getFilms() {
        log.info("Запрошен список всех фильмов");
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(int filmId) {
        log.info("Запрошен фильм с id " + filmId);
        if (!films.containsKey(filmId)) {
            log.info("Фильм с id " + filmId + " не найден");
            log.warn("Фильм с id " + filmId + " не найден");
            throw new FilmNotFoundException("Фильм с id " + filmId + " не найден");
        }
        return films.get(filmId);
    }

    private void checkFilmReleaseDate(LocalDate date) {
        if (date.isBefore(EARLIEST_FILM_RELEASE_DATE)) {
            log.warn("Дата создания фильма ожидалась от 1895-12-28, получено: " + date);
            throw new ValidationException("Некорректная дата создания фильма");
        }
    }
}
