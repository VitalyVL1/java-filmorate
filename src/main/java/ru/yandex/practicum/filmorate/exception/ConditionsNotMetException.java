package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ValidationException;

/**
 * Исключение, выбрасываемое при нарушении бизнес-условий или ограничений.
 * <p>
 * Используется в случаях, когда операция не может быть выполнена из-за несоответствия
 * определенным бизнес-правилам, но при этом входные данные формально корректны.
 * Например, попытка добавить в друзья самого себя, поставить повторный лайк
 * или выполнить действие с некорректным статусом.
 * </p>
 *
 * <p>Наследуется от {@link ValidationException}, что позволяет Spring обрабатывать
 * его как ошибку валидации и возвращать соответствующий HTTP-статус (400 Bad Request).</p>
 */
public class ConditionsNotMetException extends ValidationException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message детальное сообщение о нарушенном условии
     */
    public ConditionsNotMetException(String message) {
        super(message);
    }
}