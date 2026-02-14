package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

/**
 * REST-контроллер для управления фильмами.
 * <p>
 * Предоставляет HTTP-эндпоинты для выполнения операций CRUD с фильмами,
 * а также для управления лайками и получения популярных фильмов.
 * Все методы контроллера возвращают объекты {@link Film} и используют
 * сервисный слой {@link FilmService} для выполнения бизнес-логики.
 * </p>
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    /**
     * Получает список всех фильмов.
     *
     * @return коллекция всех фильмов, хранящихся в системе
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> findAll() {
        log.debug("Find all films");
        return filmService.findAll();
    }

    /**
     * Получает фильм по его уникальному идентификатору.
     *
     * @param id идентификатор фильма
     * @return фильм с указанным идентификатором
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм с указанным id не найден
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film findById(@PathVariable Long id) {
        log.debug("Find film by id {}", id);
        return filmService.findById(id);
    }

    /**
     * Создает новый фильм.
     * <p>
     * При создании фильм проходит валидацию на корректность полей:
     * название не может быть пустым, описание ограничено 200 символами,
     * дата релиза не может быть раньше 28 декабря 1895 года,
     * продолжительность должна быть положительной.
     * </p>
     *
     * @param film объект фильма для создания (должен быть валидным)
     * @return созданный фильм с автоматически сгенерированным идентификатором
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Create film: {}", film);
        return filmService.create(film);
    }

    /**
     * Обновляет существующий фильм.
     * <p>
     * Фильм обновляется по его идентификатору, который должен быть указан в теле запроса.
     * Все поля фильма, кроме идентификатора, могут быть изменены.
     * </p>
     *
     * @param newFilm объект фильма с обновленными данными (должен содержать корректный id)
     * @return обновленный фильм
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм с указанным id не найден
     */
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film update(@Valid @RequestBody Film newFilm) {
        log.debug("Update film: {}", newFilm);
        return filmService.update(newFilm);
    }

    /**
     * Добавляет лайк фильму от пользователя.
     *
     * @param id идентификатор фильма, которому ставится лайк
     * @param userId идентификатор пользователя, ставящего лайк
     * @return обновленный фильм с информацией о лайках
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм или пользователь не найдены
     */
    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Like film: {} by user: {}", id, userId);
        return filmService.addLike(id, userId);
    }

    /**
     * Удаляет лайк пользователя с фильма.
     *
     * @param id идентификатор фильма
     * @param userId идентификатор пользователя
     * @return обновленный фильм с информацией о лайках
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если фильм, пользователь или лайк не найдены
     */
    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Film removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Delete like film: {} by user: {}", id, userId);
        return filmService.removeLike(id, userId);
    }

    /**
     * Получает список самых популярных фильмов.
     * <p>
     * Фильмы сортируются по количеству лайков. При равенстве лайков
     * порядок не гарантируется, но обычно используется дополнительная сортировка по id.
     * </p>
     *
     * @param count количество фильмов для вывода (по умолчанию 10)
     * @return коллекция из {@code count} наиболее популярных фильмов
     */
    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> findPopular(@RequestParam(defaultValue = "10") Integer count) {
        log.debug("Find popular {} films", count);
        return filmService.findPopular(count);
    }
}