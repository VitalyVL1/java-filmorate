package ru.yandex.practicum.filmorate.exception_handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Простой DTO для формирования стандартизированного ответа с ошибкой.
 * <p>
 * Используется глобальным обработчиком исключений {@link ru.yandex.practicum.filmorate.exception_handler.controller.ErrorHandlingController}
 * для возврата клиенту информации об ошибке в единообразном формате.
 * Содержит только текстовое сообщение с описанием возникшей проблемы.
 * </p>
 *
 * <p>Применяется для большинства бизнес-исключений, не требующих детализации
 * по полям (например, {@link ru.yandex.practicum.filmorate.exception.NotFoundException},
 * {@link ru.yandex.practicum.filmorate.exception.ConditionsNotMetException}).</p>
 *
 * <p>Пример тела ответа при ошибке:
 * <pre>
 * {
 *   "message": "Фильм с id 999 не найден"
 * }
 * </pre>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.exception_handler.ValidationErrorResponse
 * @see ru.yandex.practicum.filmorate.exception_handler.controller.ErrorHandlingController
 */
@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    /**
     * Текстовое описание ошибки.
     * <p>
     * Содержит понятное для клиента сообщение о причине возникновения ошибки.
     * Обычно совпадает с сообщением из выброшенного исключения.
     * </p>
     */
    private final String message;
}