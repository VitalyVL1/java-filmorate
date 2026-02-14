package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

/**
 * REST-контроллер для работы с жанрами фильмов.
 * <p>
 * Предоставляет эндпоинты для получения справочной информации о жанрах.
 * Жанры являются неизменяемым справочником и не поддерживают операции
 * создания, обновления или удаления через API.
 * </p>
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
    private final GenreService genreService;

    /**
     * Получает список всех доступных жанров.
     * <p>
     * Возвращает полный справочник жанров, отсортированный по идентификатору.
     * Жанры предопределены и соответствуют общепринятой классификации:
     * 1 - Комедия, 2 - Драма, 3 - Мультфильм, 4 - Триллер, 5 - Документальный, 6 - Боевик.
     * </p>
     *
     * @return коллекция всех жанров, хранящихся в системе
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Genre> findAll() {
        log.debug("Find all genres");
        return genreService.findAll();
    }

    /**
     * Получает жанр по его уникальному идентификатору.
     *
     * @param id идентификатор жанра (целое число от 1 до 6)
     * @return жанр с указанным идентификатором
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если жанр с указанным id не найден
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Genre findById(@PathVariable Integer id) {
        log.debug("Find genre by id {}", id);
        return genreService.findById(id);
    }
}