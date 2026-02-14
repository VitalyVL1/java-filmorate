package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.validator.constraints.DateAfter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Модель данных, представляющая фильм.
 * <p>
 * Содержит информацию о фильме: название, описание, дату релиза, продолжительность,
 * рейтинг MPA, жанры и лайки пользователей. Используется для обмена данными
 * между слоями приложения и для валидации входных данных.
 * </p>
 *
 * <p>Валидация полей:
 * <ul>
 *   <li>{@code name} — не может быть пустым</li>
 *   <li>{@code description} — максимум 200 символов</li>
 *   <li>{@code releaseDate} — не ранее 28 декабря 1895 года</li>
 *   <li>{@code duration} — должно быть положительным числом</li>
 * </ul>
 * </p>
 */
@Data
@Slf4j
public class Film {

    /**
     * Уникальный идентификатор фильма.
     * Генерируется автоматически при создании.
     */
    private Long id;

    /**
     * Множество идентификаторов пользователей, поставивших лайк фильму.
     * Используется для подсчета популярности фильма.
     */
    private Set<Long> likes = new HashSet<>();

    /**
     * Множество жанров фильма.
     * Хранится в {@link TreeSet} с сортировкой по идентификатору жанра.
     * Фильм может относиться к нескольким жанрам одновременно.
     */
    private Set<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));

    /**
     * Возрастной рейтинг MPA (Motion Picture Association).
     * Определяет допустимую возрастную категорию зрителей.
     */
    private Mpa mpa;

    /**
     * Название фильма.
     * Обязательное поле, не может быть пустым.
     */
    @NotBlank(message = "Название не может быть пустым")
    private String name;

    /**
     * Описание фильма.
     * Опциональное поле, ограничено 200 символами.
     */
    @Length(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    /**
     * Дата релиза фильма.
     * Формат: {@code yyyy-MM-dd}. Не может быть раньше 28 декабря 1895 года
     * (дата первого кинопоказа братьев Люмьер).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateAfter
    private LocalDate releaseDate;

    /**
     * Продолжительность фильма в минутах.
     * Должна быть положительным числом.
     */
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;
}