package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class Constants {
    public static final LocalDate EARLIEST_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    public static final String DESCENDING_ORDER = "desc";
    public static final String ASCENDING_ORDER = "asc";
}
