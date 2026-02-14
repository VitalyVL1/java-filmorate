package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Модель данных, представляющая возрастной рейтинг MPA (Motion Picture Association).
 * <p>
 * Является справочной информацией и содержит предопределенный набор значений,
 * соответствующих стандартам Американской ассоциации кинокомпаний:
 * <ul>
 *   <li>1 - G (General Audiences) — нет возрастных ограничений</li>
 *   <li>2 - PG (Parental Guidance Suggested) — рекомендуется присутствие родителей</li>
 *   <li>3 - PG-13 (Parents Strongly Cautioned) — детям до 13 лет не рекомендуется</li>
 *   <li>4 - R (Restricted) — лицам до 17 лет обязательно присутствие родителей</li>
 *   <li>5 - NC-17 (Adults Only) — только для взрослых (18+)</li>
 * </ul>
 * Рейтинги не могут быть изменены или удалены через API, только просмотрены.
 * </p>
 *
 * <p>Для корректной работы в коллекциях {@link java.util.Set} используется
 * сравнение только по идентификатору через {@link EqualsAndHashCode}.</p>
 *
 * @see ru.yandex.practicum.filmorate.model.Film
 */
@Data
@Slf4j
@EqualsAndHashCode(of = {"id"})
public class Mpa {

    /**
     * Уникальный идентификатор рейтинга MPA.
     * Соответствует предопределенным значениям от 1 до 5.
     */
    private Integer id;

    /**
     * Кодовое обозначение рейтинга.
     * Обязательное поле, не может быть пустым.
     * Содержит аббревиатуру рейтинга: G, PG, PG-13, R, NC-17.
     */
    @NotBlank(message = "Название рейтинга не может быть пустым")
    private String name;

    /**
     * Развернутое описание рейтинга.
     * Содержит пояснение, для какой возрастной категории предназначен фильм
     * и при каких условиях допускается просмотр.
     */
    private String description;
}