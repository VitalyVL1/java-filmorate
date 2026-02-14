package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.validator.constraints.DateAfter;

import java.time.LocalDate;

/**
 * Реализация валидатора для аннотации {@link DateAfter}.
 * <p>
 * Проверяет, что аннотированное поле типа {@link LocalDate} содержит дату,
 * которая не раньше указанного минимального значения. Является частью системы
 * Bean Validation и автоматически вызывается при валидации объектов,
 * содержащих поля с аннотацией {@code @DateAfter}.
 * </p>
 *
 * <p>Логика работы:
 * <ul>
 *   <li>При инициализации получает минимальную допустимую дату из аннотации</li>
 *   <li>При валидации проверяет, что переданная дата не null и находится после минимальной</li>
 *   <li>Возвращает {@code true} если дата валидна, иначе {@code false}</li>
 * </ul>
 * </p>
 *
 * <p>Используется для валидации даты релиза фильма в модели {@link ru.yandex.practicum.filmorate.model.Film},
 * гарантируя, что дата не может быть раньше зарождения кинематографа
 * (28 декабря 1895 года).</p>
 *
 * @see jakarta.validation.ConstraintValidator
 * @see ru.yandex.practicum.filmorate.validator.constraints.DateAfter
 * @see ru.yandex.practicum.filmorate.model.Film
 */
public class DateAfterValidator implements ConstraintValidator<DateAfter, LocalDate> {

    /** Минимальная допустимая дата, полученная из аннотации */
    private LocalDate minDate;

    /**
     * Инициализирует валидатор, извлекая минимальную дату из аннотации.
     * <p>
     * Вызывается фреймворком валидации при создании экземпляра валидатора.
     * Преобразует строковое представление даты из аннотации в объект {@link LocalDate}.
     * </p>
     *
     * @param constraintAnnotation экземпляр аннотации {@link DateAfter}, содержащий
     *                             параметры валидации (минимальную дату)
     */
    @Override
    public void initialize(DateAfter constraintAnnotation) {
        minDate = LocalDate.parse(constraintAnnotation.minDate());
    }

    /**
     * Проверяет, что переданная дата соответствует ограничению.
     * <p>
     * Валидация считается успешной если:
     * <ul>
     *   <li>Дата не равна {@code null}</li>
     *   <li>Дата находится после {@link #minDate} (включительно? строго после)</li>
     * </ul>
     * </p>
     *
     * <p><b>Важно:</b> Метод использует строгое сравнение {@code isAfter()},
     * то есть дата, равная минимальной, считается невалидной. Это соответствует
     * требованию "не раньше указанной даты".</p>
     *
     * @param localDate проверяемая дата (может быть null)
     * @param constraintValidatorContext контекст валидации (может использоваться
     *                                   для настройки сообщения об ошибке)
     * @return {@code true} если дата валидна, иначе {@code false}
     */
    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        return localDate != null && localDate.isAfter(minDate);
    }
}