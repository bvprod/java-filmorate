package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

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
}
