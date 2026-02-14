package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс хранилища для управления возрастными рейтингами MPA.
 * <p>
 * Определяет контракт для реализации различных способов хранения данных о рейтингах MPA
 * (Motion Picture Association). Рейтинги являются справочной информацией, определяющей
 * возрастные ограничения для фильмов.
 * </p>
 *
 * <p>Стандартные рейтинги MPA (предопределенные значения):
 * <ul>
 *   <li>1 - G (General Audiences) — нет возрастных ограничений</li>
 *   <li>2 - PG (Parental Guidance Suggested) — рекомендуется присутствие родителей</li>
 *   <li>3 - PG-13 (Parents Strongly Cautioned) — детям до 13 лет не рекомендуется</li>
 *   <li>4 - R (Restricted) — лицам до 17 лет обязательно присутствие родителей</li>
 *   <li>5 - NC-17 (Adults Only) — только для взрослых (18+)</li>
 * </ul>
 * </p>
 *
 * <p>Основные операции:
 * <ul>
 *   <li>CRUD операции с рейтингами (создание, чтение, обновление, удаление)</li>
 *   <li>Проверка существования рейтинга в хранилище</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage
 * @see ru.yandex.practicum.filmorate.storage.mpa.InMemoryMpaStorage
 * @see ru.yandex.practicum.filmorate.model.Mpa
 */
public interface MpaStorage {

    /**
     * Сохраняет новый рейтинг MPA в хранилище.
     *
     * @param mpa объект рейтинга для сохранения (без идентификатора)
     * @return сохраненный рейтинг с автоматически сгенерированным идентификатором
     */
    Mpa create(Mpa mpa);

    /**
     * Возвращает рейтинг MPA по его идентификатору.
     *
     * @param id идентификатор рейтинга (для стандартных значений от 1 до 5)
     * @return {@link Optional} с найденным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не существует
     */
    Optional<Mpa> findById(Integer id);

    /**
     * Возвращает список всех рейтингов MPA из хранилища.
     *
     * @return коллекция всех рейтингов (обычно отсортирована по идентификатору)
     */
    Collection<Mpa> findAll();

    /**
     * Обновляет существующий рейтинг MPA.
     *
     * @param newMpa объект рейтинга с обновленными данными (должен содержать корректный id)
     * @return {@link Optional} с обновленным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не найден
     */
    Optional<Mpa> update(Mpa newMpa);

    /**
     * Удаляет рейтинг MPA по его идентификатору.
     *
     * @param id идентификатор рейтинга
     * @return {@link Optional} с удаленным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не найден
     */
    Optional<Mpa> removeById(Integer id);

    /**
     * Проверяет, существует ли указанный рейтинг MPA в хранилище.
     * <p>
     * Используется для валидации при добавлении рейтингов к фильмам.
     * Сравнение происходит по всем полям через {@link Mpa#equals(Object)}.
     * </p>
     *
     * @param mpa рейтинг для проверки
     * @return {@code true} если такой рейтинг существует в хранилище, иначе {@code false}
     */
    boolean contains(Mpa mpa);
}