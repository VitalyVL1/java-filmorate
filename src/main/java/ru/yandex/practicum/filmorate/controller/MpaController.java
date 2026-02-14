package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

/**
 * REST-контроллер для работы с рейтингами MPA (Motion Picture Association).
 * <p>
 * Предоставляет эндпоинты для получения справочной информации о возрастных рейтингах.
 * Рейтинги MPA являются неизменяемым справочником и не поддерживают операции
 * создания, обновления или удаления через API. Соответствуют стандартам
 * Американской ассоциации кинокомпаний.
 * </p>
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    /**
     * Получает список всех возрастных рейтингов MPA.
     * <p>
     * Возвращает полный справочник рейтингов, отсортированный по идентификатору.
     * Рейтинги предопределены и соответствуют стандартной классификации MPA:
     * 1 - G (General Audiences), 2 - PG (Parental Guidance Suggested),
     * 3 - PG-13 (Parents Strongly Cautioned), 4 - R (Restricted),
     * 5 - NC-17 (Adults Only).
     * </p>
     *
     * @return коллекция всех рейтингов MPA, хранящихся в системе
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Mpa> findAll() {
        log.debug("Find all mpa");
        return mpaService.findAll();
    }

    /**
     * Получает рейтинг MPA по его уникальному идентификатору.
     *
     * @param id идентификатор рейтинга MPA (целое число от 1 до 5)
     * @return рейтинг MPA с указанным идентификатором
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если рейтинг с указанным id не найден
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mpa findById(@PathVariable Integer id) {
        log.debug("Find mpa by id {}", id);
        return mpaService.findById(id);
    }
}