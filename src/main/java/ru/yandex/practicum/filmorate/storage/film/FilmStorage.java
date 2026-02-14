package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс хранилища для управления фильмами.
 * <p>
 * Определяет контракт для реализации различных способов хранения данных о фильмах
 * (например, в памяти или в базе данных). Все операции с фильмами должны проходить
 * через этот интерфейс, что позволяет легко переключаться между разными реализациями
 * хранилища без изменения бизнес-логики.
 * </p>
 *
 * <p>Основные операции:
 * <ul>
 *   <li>CRUD операции с фильмами (создание, чтение, обновление, удаление)</li>
 *   <li>Управление лайками пользователей</li>
 *   <li>Получение популярных фильмов по количеству лайков</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.storage.film.FilmDbStorage
 * @see ru.yandex.practicum.filmorate.model.Film
 */
public interface FilmStorage {

    /**
     * Сохраняет новый фильм в хранилище.
     *
     * @param film объект фильма для сохранения (без идентификатора)
     * @return сохраненный фильм с автоматически сгенерированным идентификатором
     */
    Film create(Film film);

    /**
     * Возвращает фильм по его идентификатору.
     *
     * @param id идентификатор фильма
     * @return {@link Optional} с найденным фильмом, или пустой {@link Optional},
     *         если фильм с указанным id не существует
     */
    Optional<Film> findById(Long id);

    /**
     * Возвращает список всех фильмов из хранилища.
     *
     * @return коллекция всех фильмов (порядок не гарантирован)
     */
    Collection<Film> findAll();

    /**
     * Обновляет существующий фильм.
     *
     * @param newFilm объект фильма с обновленными данными (должен содержать корректный id)
     * @return {@link Optional} с обновленным фильмом, или пустой {@link Optional},
     *         если фильм с указанным id не найден
     */
    Optional<Film> update(Film newFilm);

    /**
     * Удаляет фильм по его идентификатору.
     *
     * @param id идентификатор фильма
     * @return {@link Optional} с удаленным фильмом, или пустой {@link Optional},
     *         если фильм с указанным id не найден
     */
    Optional<Film> removeById(Long id);

    /**
     * Добавляет лайк фильму от пользователя.
     *
     * @param film фильм, которому ставится лайк
     * @param user пользователь, ставящий лайк
     * @return обновленный фильм с информацией о лайках
     */
    Film addLike(Film film, User user);

    /**
     * Удаляет лайк пользователя с фильма.
     *
     * @param film фильм, с которого удаляется лайк
     * @param user пользователь, удаляющий лайк
     * @return обновленный фильм с информацией о лайках
     */
    Film removeLike(Film film, User user);

    /**
     * Возвращает список наиболее популярных фильмов.
     * <p>
     * Фильмы сортируются по убыванию количества лайков.
     * </p>
     *
     * @param limit максимальное количество фильмов для возврата
     * @return коллекция из {@code limit} наиболее популярных фильмов
     */
    Collection<Film> findPopular(Integer limit);
}