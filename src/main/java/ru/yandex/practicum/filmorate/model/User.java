package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    private int id;
    @Email(message = "Поле email должно содержать корректный адрес электронной почты")
    @NotBlank(message = "Поле email должно быть заполнено")
    private String email;
    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;
    private String name;
    @PastOrPresent(message = "День рождения должен быть датой в прошлом")
    private LocalDate birthday;
    private Set<User> friends;

    public boolean addFriend(User user) {
        return friends.add(user);
    }

    public boolean removeFriend(User user) {
        return friends.remove(user);
    }
}
