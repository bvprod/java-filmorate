package ru.yandex.practicum.filmorate.model.comparators;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;

import static ru.yandex.practicum.filmorate.storage.Constants.DESCENDING_ORDER;

public class FilmComparator implements Comparator<Film> {

    @Override
    public int compare(Film film1, Film film2) {
        return film1.getLikes().size() - film2.getLikes().size();
    }

    public int compare(Film film1, Film film2, String sortOrder) {
        int result = film1.getLikes().size() - film2.getLikes().size();
        if (sortOrder.equals(DESCENDING_ORDER)) {
            result = -1 * result;
        }
        return result;
    }
}
