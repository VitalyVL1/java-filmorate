package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@Slf4j
public class User {
    private Long id;

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
