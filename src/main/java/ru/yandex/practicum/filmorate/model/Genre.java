package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Модель данных, представляющая жанр фильма.
 * <p>
 * Является справочной информацией и содержит предопределенный набор значений:
 * <ul>
 *   <li>1 - Комедия</li>
 *   <li>2 - Драма</li>
 *   <li>3 - Мультфильм</li>
 *   <li>4 - Триллер</li>
 *   <li>5 - Документальный</li>
 *   <li>6 - Боевик</li>
 * </ul>
 * Жанры не могут быть изменены или удалены через API, только просмотрены.
 * </p>
 *
 * <p>Жанры хранятся в {@link Film#genres} с сортировкой по {@link #id}.
 * Для корректной работы в коллекциях {@link java.util.Set} используется
 * сравнение только по идентификатору через {@link EqualsAndHashCode}.</p>
 */
@Data
@Slf4j
@EqualsAndHashCode(of = {"id"})
public class Genre {

    /**
     * Уникальный идентификатор жанра.
     * Соответствует предопределенным значениям от 1 до 6.
     */
    private Integer id;

    /**
     * Название жанра.
     * Обязательное поле, не может быть пустым. Соответствует названиям
     * предопределенных жанров (например, "Комедия", "Драма").
     */
    @NotBlank(message = "Название жанра не может быть пустым")
    private String name;
}