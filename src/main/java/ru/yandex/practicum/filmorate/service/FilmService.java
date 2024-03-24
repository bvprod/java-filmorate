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

    public boolean addLike(Film film, User user) {
        return film.addLike(user);
    }

    public boolean removeLike(Film film, User user) {
        return film.removeLike(user);
    }

    public List<Film> getPopularFilms(int count, String sortingOrder) {
        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> compareFilmsByPopularity(f1, f2, sortingOrder))
                .limit(count)
                .collect(Collectors.toList());
    }

    private int compareFilmsByPopularity(Film film1, Film film2, String sort) {
        int result = film1.getLikes().size() - film2.getLikes().size(); //сортировка по убыванию
        if (sort.equals(ASCENDING_ORDER)) {
            result = -1 * result; //сортировка по возрастанию
        }
        return result;
    }
}
