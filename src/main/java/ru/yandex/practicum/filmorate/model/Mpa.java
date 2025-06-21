package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Mpa {
    private Integer id;

    @NotBlank(message = "Название рейтинга не может быть пустым")
    private String name;
    private String description;
}
