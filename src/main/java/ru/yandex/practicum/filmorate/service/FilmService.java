package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.Exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.Constants.DESCENDING_ORDER;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserService userService;
    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        try {
            filmStorage.getFilm(film.getId());
        } catch (FilmNotFoundException e) {
            throw e;
        }
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public List<Film> getPopularFilms(int count, String sortingOrder) {
        log.info("Запрошен список " + count + " популярных фильмов. Сортировка " + sortingOrder);
        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> compareFilmsByPopularity(f1, f2, sortingOrder))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenre(int genreId) {
        return filmStorage.getGenre(genreId);
    }

    public Film getFilm(int filmId) {
        log.info("Запрошен фильм " + filmId);
        return filmStorage.getFilm(filmId);
    }

    private int compareFilmsByPopularity(Film film1, Film film2, String sort) {
        int result = film1.getLikes().size() - film2.getLikes().size(); //сортировка по возрастанию
        if (sort.equals(DESCENDING_ORDER)) {
            result = -1 * result; //сортировка по убыванию
        }
        return result;
    }

    public Film addLike(int filmId, int userId) {
        try {
            filmStorage.getFilm(filmId);
            userService.getUser(userId);
        } catch (UserNotFoundException | FilmNotFoundException e) {
           throw e;
        }
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(int filmId, int userId) {
        try {
            filmStorage.getFilm(filmId);
            userService.getUser(userId);
        } catch (UserNotFoundException | FilmNotFoundException e) {
            throw e;
        }
        return filmStorage.removeLike(filmId, userId);
    }

    public Mpa getRating(int ratingId) {
        return filmStorage.getRating(ratingId);
    }

    public List<Mpa> getAllRatings() {
        return filmStorage.getAllRatings();
    }
}
