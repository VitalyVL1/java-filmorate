package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@EqualsAndHashCode(of = {"id"})
public class Genre {
    Integer id;

    @NotBlank(message = "Название жанра не может быть пустым")
    private String name;
}
