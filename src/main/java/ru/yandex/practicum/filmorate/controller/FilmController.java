package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final HashSet<Film> films = new HashSet<>();

    private int idCounter = 1;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            log.warn("Дата создания фильма ожидалась от 1895-12-28, получено: " + film.getReleaseDate());
            throw new ValidationException("Некорректная дата создания фильма");
        } else {
            film.setId(idCounter++);
            films.add(film);
            log.info("Фильм " + film.getId() + " добавлен. Всего фильмов в коллекции: " + films.size());
            return film;
        }
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            log.warn("Дата создания фильма ожидалась от 1895-12-28, получено: " + film.getReleaseDate());
            throw new ValidationException("Некорректная дата создания фильма");
        } else if (films.contains(film)) {
            films.remove(film);
            films.add(film);
            log.info("Фильм " + film.getId() + " обновлен");
            return film;
        } else {
            log.warn("Фильм с id " + film.getId() + " не найден");
            throw new ValidationException("Фильм с id " + film.getId() + " не найден");
        }
    }

    @GetMapping
    public Set<Film> getFilms() {
        return films;
    }
}
