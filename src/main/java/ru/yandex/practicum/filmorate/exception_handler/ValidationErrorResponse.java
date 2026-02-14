package ru.yandex.practicum.filmorate.exception_handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * DTO для формирования стандартизированного ответа с детализированными ошибками валидации.
 * <p>
 * Используется глобальным обработчиком исключений {@link ru.yandex.practicum.filmorate.exception_handler.controller.ErrorHandlingController}
 * для возврата клиенту подробной информации об ошибках валидации с указанием конкретных полей,
 * вызвавших нарушение. В отличие от простого {@link ErrorResponse}, позволяет передавать
 * список нарушений с детализацией по каждому проблемному полю.
 * </p>
 *
 * <p>Применяется для обработки исключений {@link org.springframework.web.bind.MethodArgumentNotValidException}
 * (ошибки валидации тела запроса) и {@link jakarta.validation.ConstraintViolationException}
 * (ошибки валидации параметров запроса).</p>
 *
 * <p>Пример тела ответа при ошибке валидации:
 * <pre>
 * {
 *   "violations": [
 *     {
 *       "fieldName": "email",
 *       "message": "Email должен быть корректным"
 *     },
 *     {
 *       "fieldName": "login",
 *       "message": "Логин не может содержать пробелы"
 *     }
 *   ]
 * }
 * </pre>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.exception_handler.ErrorResponse
 * @see ru.yandex.practicum.filmorate.exception_handler.Violation
 * @see ru.yandex.practicum.filmorate.exception_handler.controller.ErrorHandlingController
 */
@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {

    /**
     * Список нарушений, обнаруженных при валидации.
     * <p>
     * Каждый элемент списка содержит информацию об одном проблемном поле:
     * название поля и сообщение об ошибке. Позволяет клиенту точно определить,
     * какие именно данные были некорректными.
     * </p>
     */
    private final List<Violation> violations;
}