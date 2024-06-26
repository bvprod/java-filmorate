package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

import static ru.yandex.practicum.filmorate.storage.Constants.DESCENDING_ORDER;
import static ru.yandex.practicum.filmorate.storage.Constants.SORTS;

@RestController
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping("/films")
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/films")
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/films/{filmId}")
    public Film getFilmById(@PathVariable("filmId") Integer filmId) {
        return filmService.getFilm(filmId);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public Film addFilmLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public Film removeFilmLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        return filmService.removeLike(filmId, userId);
    }

    @GetMapping("/films/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10", required = false) Integer size,
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

    @GetMapping("/genres")
    public List<Genre> getPopularFilms() {
        return filmService.getAllGenres();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenreById(@PathVariable("id") Integer genreId) {
        return filmService.getGenre(genreId);
    }

    @GetMapping("/mpa/{id}")
    public Mpa getRatingById(@PathVariable("id") Integer ratingId) {
        return filmService.getRating(ratingId);
    }

    @GetMapping("/mpa")
    public List<Mpa> getAllRatings() {
        return filmService.getAllRatings();
    }
}
