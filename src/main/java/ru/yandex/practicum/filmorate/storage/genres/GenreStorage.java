package ru.yandex.practicum.filmorate.storage.genres;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс хранилища для управления жанрами фильмов.
 * <p>
 * Определяет контракт для реализации различных способов хранения данных о жанрах
 * (например, в памяти или в базе данных). Жанры являются справочной информацией,
 * но интерфейс поддерживает полный CRUD для возможности расширения функционала.
 * </p>
 *
 * <p>Основные операции:
 * <ul>
 *   <li>CRUD операции с жанрами (создание, чтение, обновление, удаление)</li>
 *   <li>Проверка существования жанра в хранилище</li>
 * </ul>
 * </p>
 *
 * <p>В текущей реализации приложения жанры являются предопределенным справочником
 * (значения от 1 до 6), но данный интерфейс позволяет в будущем реализовать
 * динамическое управление жанрами.</p>
 *
 * @see ru.yandex.practicum.filmorate.storage.genres.GenreDbStorage
 * @see ru.yandex.practicum.filmorate.storage.genres.InMemoryGenreStorage
 * @see ru.yandex.practicum.filmorate.model.Genre
 */
public interface GenreStorage {

    /**
     * Сохраняет новый жанр в хранилище.
     *
     * @param genre объект жанра для сохранения (без идентификатора)
     * @return сохраненный жанр с автоматически сгенерированным идентификатором
     */
    Genre create(Genre genre);

    /**
     * Возвращает жанр по его идентификатору.
     *
     * @param id идентификатор жанра (для предопределенных значений от 1 до 6)
     * @return {@link Optional} с найденным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не существует
     */
    Optional<Genre> findById(Integer id);

    /**
     * Возвращает список всех жанров из хранилища.
     *
     * @return коллекция всех жанров (обычно отсортирована по идентификатору)
     */
    Collection<Genre> findAll();

    /**
     * Обновляет существующий жанр.
     *
     * @param newGenre объект жанра с обновленными данными (должен содержать корректный id)
     * @return {@link Optional} с обновленным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не найден
     */
    Optional<Genre> update(Genre newGenre);

    /**
     * Удаляет жанр по его идентификатору.
     *
     * @param id идентификатор жанра
     * @return {@link Optional} с удаленным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не найден
     */
    Optional<Genre> removeById(Integer id);

    /**
     * Проверяет, существует ли указанный жанр в хранилище.
     * <p>
     * Используется для валидации при добавлении жанров к фильмам.
     * Сравнение происходит по всем полям через {@link Genre#equals(Object)}.
     * </p>
     *
     * @param genre жанр для проверки
     * @return {@code true} если такой жанр существует в хранилище, иначе {@code false}
     */
    boolean contains(Genre genre);
}