package ru.yandex.practicum.filmorate.exception_handler.controller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception_handler.ErrorResponse;
import ru.yandex.practicum.filmorate.exception_handler.ValidationErrorResponse;
import ru.yandex.practicum.filmorate.exception_handler.Violation;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для REST-контроллеров приложения.
 * <p>
 * Централизованно перехватывает исключения, возникающие в процессе обработки
 * HTTP-запросов, и преобразует их в стандартизированные ответы с соответствующими
 * HTTP-статусами. Обеспечивает единообразный формат ошибок для всего API.
 * </p>
 *
 * <p>Все методы обработчиков логируют ошибки и возвращают объекты ответов
 * в формате {@link ErrorResponse} (для простых ошибок) или
 * {@link ValidationErrorResponse} (для ошибок валидации с детализацией по полям).</p>
 */
@RestControllerAdvice
@Slf4j
public class ErrorHandlingController {

    /**
     * Обрабатывает исключения валидации параметров запроса (path variables, request params).
     * <p>
     * Возникает при нарушении ограничений, заданных аннотациями валидации
     * (например, {@code @Min}, {@code @NotNull}) для параметров методов контроллеров.
     * </p>
     *
     * @param e исключение {@link ConstraintViolationException}
     * @return объект {@link ValidationErrorResponse} со списком нарушений,
     *         содержащим путь к параметру и сообщение об ошибке
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onConstraintValidationException(ConstraintViolationException e) {
        log.error(e.getMessage(), e);

        final List<Violation> violations = e.getConstraintViolations().stream()
                .map(
                        violation -> new Violation(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .collect(Collectors.toList());

        return new ValidationErrorResponse(violations);
    }

    /**
     * Обрабатывает исключения валидации тела запроса (объектов {@code @RequestBody}).
     * <p>
     * Возникает при нарушении ограничений валидации в полях объектов,
     * помеченных аннотацией {@code @Valid}.
     * </p>
     *
     * @param e исключение {@link MethodArgumentNotValidException}
     * @return объект {@link ValidationErrorResponse} со списком нарушений,
     *         содержащим название поля и сообщение об ошибке
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);

        final List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ValidationErrorResponse(violations);
    }

    /**
     * Обрабатывает исключения, связанные с отсутствием запрашиваемого ресурса.
     *
     * @param e исключение {@link NotFoundException}
     * @return объект {@link ErrorResponse} с сообщением об ошибке
     * @see NotFoundException
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse onNotFoundException(NotFoundException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с нарушением бизнес-условий.
     *
     * @param e исключение {@link ConditionsNotMetException}
     * @return объект {@link ErrorResponse} с сообщением об ошибке
     * @see ConditionsNotMetException
     */
    @ExceptionHandler(ConditionsNotMetException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onConditionsNotMetException(ConditionsNotMetException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с попыткой создания дублирующихся данных.
     *
     * @param e исключение {@link DuplicatedDataException}
     * @return объект {@link ErrorResponse} с сообщением об ошибке
     * @see DuplicatedDataException
     */
    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onDuplicatedDataException(DuplicatedDataException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    /**
     * Обрабатывает исключения, возникающие при парсинге дат.
     * <p>
     * Возникает, когда строка с датой не соответствует требуемому формату {@code yyyy-MM-dd}.
     * </p>
     *
     * @param e исключение {@link DateTimeParseException}
     * @return объект {@link ErrorResponse} с сообщением об ошибке, содержащим
     *         некорректное значение даты
     */
    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onDateTimeParseException(DateTimeParseException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Дата должна быть в формате yyyy-MM-dd, вами введено: " + e.getParsedString());
    }

    /**
     * Обрабатывает внутренние ошибки сервера.
     * <p>
     * Используется для критических ошибок, не связанных с действиями пользователя
     * (проблемы с БД, SQL-запросами и т.д.).
     * </p>
     *
     * @param e исключение {@link InternalServerException}
     * @return объект {@link ErrorResponse} с сообщением об ошибке
     * @see InternalServerException
     */
    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse onInternalServerException(InternalServerException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }
}