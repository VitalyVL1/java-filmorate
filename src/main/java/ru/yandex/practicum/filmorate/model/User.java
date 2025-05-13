package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private final Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Введен некорректный email")
    private String email;

    @NotBlank(message = "логин не может быть пустым")
    @Pattern(regexp = "^\\S*$", message = "логин не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
