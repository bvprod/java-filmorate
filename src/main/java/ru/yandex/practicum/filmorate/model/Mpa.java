package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mpa {
    @EqualsAndHashCode.Include
    private int id;
    private String name;

    public Mpa(int id, String ratingTitle) {
        this.id = id;
        this.name = ratingTitle;
    }
}
