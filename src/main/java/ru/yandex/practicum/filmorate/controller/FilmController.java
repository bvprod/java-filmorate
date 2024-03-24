package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.*;

import static ru.yandex.practicum.filmorate.storage.Constants.DESCENDING_ORDER;
import static ru.yandex.practicum.filmorate.storage.Constants.SORTS;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmStorage.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable("filmId") Integer filmId) {
        return filmStorage.getFilm(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addFilmLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeFilmLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10" ,required = false) Integer size,
            @RequestParam(defaultValue = DESCENDING_ORDER, required = false) String sortingOrder
    ) {
        if (!SORTS.contains(sortingOrder)) {
            throw new IncorrectParameterException("sort");
        }
        if (size <= 0) {
            throw new IncorrectParameterException("page");
        }
        return filmService.getPopularFilms(size, sortingOrder);
    }
}
