package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.deserializers.GenreDeserializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film {
    @EqualsAndHashCode.Include
    private int id;
    @NotBlank(message = "Имя не должно быть пустым")
    private String name;
    @Size(max = 200, message = "Длина описания не должна превышать 200 символов")
    private String description;
    @PastOrPresent(message = "Дата выхода фильма должна быть датой в прошлом")
    private LocalDate releaseDate;
    @Positive(message = "Длительность фильма должна быть положительным числом")
    private int duration;
    private Set<Integer> likes = new HashSet<>();
    //@JsonDeserialize(using = GenreDeserializer.class)
    private List<Genre> genres;
    private Mpa mpa;
}
