package ru.yandex.practicum.filmorate.exception_handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DTO для представления одного нарушения валидации.
 * <p>
 * Используется в составе {@link ValidationErrorResponse} для детализации ошибок
 * валидации по конкретным полям. Содержит информацию о том, какое поле
 * не прошло валидацию и какое именно ограничение было нарушено.
 * </p>
 *
 * <p>Пример использования в JSON-ответе:
 * <pre>
 * {
 *   "fieldName": "email",
 *   "message": "Email должен быть корректным"
 * }
 * </pre>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.exception_handler.ValidationErrorResponse
 * @see ru.yandex.practicum.filmorate.exception_handler.controller.ErrorHandlingController
 */
@Getter
@RequiredArgsConstructor
public class Violation {

    /**
     * Название поля, которое не прошло валидацию.
     * <p>
     * Для параметров пути (path variables) может содержать полный путь к параметру,
     * например "findById.id". Для полей тела запроса содержит просто имя поля,
     * например "email" или "login".
     * </p>
     */
    private final String fieldName;

    /**
     * Сообщение об ошибке валидации.
     * <p>
     * Описывает, какое именно ограничение было нарушено. Сообщение обычно
     * формируется на основе аннотаций валидации (например, "не должно быть пустым",
     * "должно быть корректным email адресом") и может быть локализовано.
     * </p>
     */
    private final String message;
}