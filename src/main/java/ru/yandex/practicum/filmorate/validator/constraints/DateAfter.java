package ru.yandex.practicum.filmorate.validator.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validator.DateAfterValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Кастомная аннотация для валидации дат с проверкой на минимальное допустимое значение.
 * <p>
 * Используется для проверки, что аннотированное поле типа {@link java.time.LocalDate}
 * содержит дату не ранее указанной минимальной даты. По умолчанию проверяет,
 * что дата не раньше 28 декабря 1895 года (дата первого кинопоказа братьев Люмьер,
 * принятая как начало эпохи кинематографа).
 * </p>
 *
 * <p>Применяется в модели {@link ru.yandex.practicum.filmorate.model.Film}
 * для поля {@code releaseDate}, гарантируя, что дата релиза фильма
 * не может быть раньше зарождения кинематографа.
 * </p>
 *
 * <p>Пример использования:
 * <pre>
 * {@code
 * @DateAfter
 * private LocalDate releaseDate;
 *
 * @DateAfter(minDate = "2000-01-01", message = "Дата должна быть не раньше 2000 года")
 * private LocalDate customDate;
 * }
 * </pre>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.validator.DateAfterValidator
 * @see ru.yandex.practicum.filmorate.model.Film
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateAfterValidator.class)
public @interface DateAfter {

    /**
     * Минимальная допустимая дата в формате {@code yyyy-MM-dd}.
     * <p>
     * По умолчанию установлена дата первого кинопоказа братьев Люмьер
     * (28 декабря 1895 года). Может быть переопределена для других
     * бизнес-требований.
     * </p>
     *
     * @return строковое представление минимальной даты в формате ISO
     */
    String minDate() default "1895-12-28";

    /**
     * Сообщение об ошибке валидации.
     * <p>
     * Возвращается в ответе API при нарушении ограничения.
     * По умолчанию: "Дата должна быть не раньше 28 декабря 1895".
     * </p>
     *
     * @return текст сообщения об ошибке
     */
    String message() default "Дата должна быть не раньше 28 декабря 1895";

    /**
     * Группы валидации (для группировки ограничений).
     * По умолчанию пустой массив.
     *
     * @return группы валидации
     */
    Class<?>[] groups() default {};

    /**
     * Параметры валидации (для расширенной конфигурации).
     * По умолчанию пустой массив.
     *
     * @return массив классов с payload
     */
    Class<? extends Payload>[] payload() default {};
}