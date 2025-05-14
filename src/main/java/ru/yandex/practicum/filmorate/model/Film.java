package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.time.DurationMin;
import ru.yandex.practicum.filmorate.validator.constraints.DateAfter;

import java.time.Duration;
import java.time.LocalDate;

@Data
@Slf4j
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Length(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @DateAfter
    private LocalDate releaseDate;

    @DurationMin(message = "Продолжительность фильма должна быть положительным числом")
    private Duration duration;
}
