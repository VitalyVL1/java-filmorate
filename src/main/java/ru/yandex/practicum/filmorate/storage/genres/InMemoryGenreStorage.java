package ru.yandex.practicum.filmorate.storage.genres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.util.GenreUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Реализация хранилища жанров на основе оперативной памяти (in-memory).
 * <p>
 * Обеспечивает временное хранение данных о жанрах, используя {@link Map}
 * для хранения объектов. При инициализации заполняется предопределенными
 * значениями жанров (1-6) через утилитный класс {@link GenreUtil}.
 * Предназначен для разработки и тестирования.
 * </p>
 *
 * <p>Предопределенные жанры:
 * <ul>
 *   <li>1 - Комедия</li>
 *   <li>2 - Драма</li>
 *   <li>3 - Мультфильм</li>
 *   <li>4 - Триллер</li>
 *   <li>5 - Документальный</li>
 *   <li>6 - Боевик</li>
 * </ul>
 * </p>
 *
 * <p><b>Важно:</b> Данная реализация не подходит для production-среды,
 * так как все данные теряются при остановке приложения. Используется
 * на этапе разработки для быстрого прототипирования или как источник
 * данных по умолчанию.</p>
 *
 * @see ru.yandex.practicum.filmorate.storage.genres.GenreStorage
 * @see ru.yandex.practicum.filmorate.storage.genres.GenreDbStorage
 * @see ru.yandex.practicum.filmorate.util.GenreUtil
 */
@Slf4j
@Component
public class InMemoryGenreStorage implements GenreStorage {

    /**
     * Хранилище жанров в памяти.
     * Ключ - идентификатор жанра, значение - объект жанра.
     * Инициализируется предопределенными значениями через {@link GenreUtil#fillGenres()}.
     */
    private final Map<Integer, Genre> genres = GenreUtil.fillGenres();

    /**
     * Создает новый жанр в in-memory хранилище.
     * <p>
     * Генерирует новый идентификатор для жанра (начиная с 7, так как
     * предопределенные жанры занимают ID 1-6) и добавляет его в хранилище.
     * </p>
     *
     * @param genre объект жанра для сохранения
     * @return сохраненный жанр с заполненным идентификатором
     */
    @Override
    public Genre create(Genre genre) {
        genre.setId(getNextId());
        genres.put(genre.getId(), genre);
        return genre;
    }

    /**
     * Находит жанр по его идентификатору.
     *
     * @param id идентификатор жанра (для предопределенных значений от 1 до 6)
     * @return {@link Optional} с найденным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не существует
     */
    @Override
    public Optional<Genre> findById(Integer id) {
        return Optional.ofNullable(genres.get(id));
    }

    /**
     * Возвращает список всех жанров.
     * <p>
     * Возвращает коллекцию, содержащую как предопределенные жанры (1-6),
     * так и добавленные пользователем через метод {@link #create}.
     * </p>
     *
     * @return коллекция всех жанров в памяти
     */
    @Override
    public Collection<Genre> findAll() {
        return genres.values();
    }

    /**
     * Обновляет существующий жанр.
     * <p>
     * Если жанр с указанным ID существует, обновляет его название
     * (при условии, что новое название не пустое). Предопределенные жанры
     * также могут быть обновлены.
     * </p>
     *
     * @param newGenre объект жанра с обновленными данными
     * @return {@link Optional} с обновленным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не найден
     */
    @Override
    public Optional<Genre> update(Genre newGenre) {
        if (!genres.containsKey(newGenre.getId())) {
            return Optional.empty();
        }

        Genre oldGenre = genres.get(newGenre.getId());

        if (newGenre.getName() != null && !newGenre.getName().isBlank()) {
            oldGenre.setName(newGenre.getName());
            genres.put(oldGenre.getId(), oldGenre);
        }

        return Optional.of(oldGenre);
    }

    /**
     * Удаляет жанр по его идентификатору.
     * <p>
     * <b>Внимание:</b> Позволяет удалять даже предопределенные жанры (1-6),
     * что может нарушить работу приложения. В текущей реализации не рекомендуется
     * удалять предопределенные значения.
     * </p>
     *
     * @param id идентификатор жанра
     * @return {@link Optional} с удаленным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не найден
     */
    @Override
    public Optional<Genre> removeById(Integer id) {
        return Optional.ofNullable(genres.remove(id));
    }

    /**
     * Проверяет, существует ли жанр с указанным идентификатором.
     * <p>
     * В отличие от {@link GenreStorage#contains}, эта реализация проверяет
     * только наличие по ID, а не полное равенство объектов.
     * </p>
     *
     * @param genre жанр для проверки
     * @return {@code true} если жанр с таким ID существует, иначе {@code false}
     */
    @Override
    public boolean contains(Genre genre) {
        return genres.containsKey(genre.getId());
    }

    /**
     * Генерирует следующий уникальный идентификатор для нового жанра.
     * <p>
     * Находит максимальный существующий ID и увеличивает его на 1.
     * Так как предопределенные жанры занимают ID 1-6, новые жанры
     * будут получать ID начиная с 7.
     * </p>
     *
     * @return следующий уникальный идентификатор
     */
    private Integer getNextId() {
        int currentMaxId = genres.keySet()
                .stream()
                .reduce(Integer::max)
                .orElse(0);
        return ++currentMaxId;
    }
}