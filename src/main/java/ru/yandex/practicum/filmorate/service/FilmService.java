package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.storage.Constants.ASCENDING_ORDER;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film addLike(int filmId, int userId) {
       Film film = getFilm(filmId);
       film.addLike(userId);
       return film;
    }

    public Film removeLike(int filmId, int userId) {
        Film film = getFilm(filmId);
        film.removeLike(userId);
        return film;
    }

    public List<Film> getPopularFilms(int count, String sortingOrder) {
        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> compareFilmsByPopularity(f1, f2, sortingOrder))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilm(int filmId) {
        return filmStorage.getFilm(filmId);
    }

    private int compareFilmsByPopularity(Film film1, Film film2, String sort) {
        int result = film1.getLikes().size() - film2.getLikes().size(); //сортировка по убыванию
        if (sort.equals(ASCENDING_ORDER)) {
            result = -1 * result; //сортировка по возрастанию
        }
        return result;
    }
}
