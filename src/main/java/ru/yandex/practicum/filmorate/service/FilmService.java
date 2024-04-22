package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.comparators.FilmComparator;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

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
        log.info("Запрос на добавление фильма " + film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Запрос на обновление фильма " + film.getName());
        filmStorage.getFilm(film.getId());
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        log.info("Запрос на получение списка всех фильмов");
        return filmStorage.getFilms();
    }

    public List<Genre> getAllGenres() {
        log.info("Запрос на получение списка всех жанров");
        return filmStorage.getAllGenres();
    }

    public Genre getGenre(int genreId) {
        log.info("Запрос на получение жанра с id = " + genreId);
        return filmStorage.getGenre(genreId);
    }

    public Film getFilm(int filmId) {
        log.info("Запрошен фильм с id = " + filmId);
        return filmStorage.getFilm(filmId);
    }

    public Film addLike(int filmId, int userId) {
        log.info("Запрос на лайк фильму " + filmId + " от " + userId);
        filmStorage.getFilm(filmId);
        userService.getUser(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(int filmId, int userId) {
        log.info("Запрос на удаление лайка фильму " + filmId + " от " + userId);
        filmStorage.getFilm(filmId);
        userService.getUser(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public Mpa getRating(int ratingId) {
        log.info("Запрос на получение рейтинга под id = " + ratingId);
        return filmStorage.getRating(ratingId);
    }

    public List<Mpa> getAllRatings() {
        log.info("Запрос на получение списка рейтингов");
        return filmStorage.getAllRatings();
    }

    public List<Film> getPopularFilms(int count, String sortingOrder) {
        log.info("Запрошен список " + count + " популярных фильмов. Сортировка " + sortingOrder);
        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> new FilmComparator().compare(f1, f2, sortingOrder))
                .limit(count)
                .collect(Collectors.toList());
    }
}
