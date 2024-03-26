package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
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
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Integer filmId) {
        return filmStorage.getFilm(filmId);
    }

    public Film addLike(int filmId, int userId) {
        Film film = getFilm(filmId);
        userService.getUser(userId); //throws error if user not found
        film.addLike(userId);
        log.info("Поставлен лайк фильму " + filmId + " от пользователя " + userId);
        return film;
    }

    public Film removeLike(int filmId, int userId) {
        Film film = getFilm(filmId);
        userService.getUser(userId); //throws error if user not found
        film.removeLike(userId);
        log.info("Удален лайк по фильму " + filmId + " от пользователя " + userId);
        return film;
    }

    public List<Film> getPopularFilms(int count, String sortingOrder) {
        log.info("Запрошен список " + count + " популярных фильмов. Сортировка " + sortingOrder);
        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> compareFilmsByPopularity(f1, f2, sortingOrder))
                .limit(count)
                .collect(Collectors.toList());
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
}
