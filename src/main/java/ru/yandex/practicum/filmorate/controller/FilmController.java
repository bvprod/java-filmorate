package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final HashMap<Integer, Film> films = new HashMap<>();

    private int idCounter = 1;

    private static final LocalDate earliestFilm = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        checkFilmReleaseDate(film.getReleaseDate());
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Фильм " + film.getId() + " добавлен. Всего фильмов в коллекции: " + films.size());
        return film;

    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        checkFilmReleaseDate(film.getReleaseDate());
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм " + film.getId() + " обновлен");
            return film;
        } else {
            log.warn("Фильм с id " + film.getId() + " не найден");
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    private void checkFilmReleaseDate(LocalDate date) {
        if (date.isBefore(earliestFilm)) {
            log.warn("Дата создания фильма ожидалась от 1895-12-28, получено: " + date);
            throw new ValidationException("Некорректная дата создания фильма");
        }
    }
}
