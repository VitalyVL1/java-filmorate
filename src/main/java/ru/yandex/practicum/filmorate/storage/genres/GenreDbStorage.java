package ru.yandex.practicum.filmorate.storage.genres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * Реализация хранилища жанров на основе базы данных (H2).
 * <p>
 * Обеспечивает持久ение (persistence) данных о жанрах фильмов в реляционной базе данных.
 * Использует {@link JdbcTemplate} для выполнения SQL-запросов и {@link RowMapper}
 * для преобразования результатов запросов в объекты {@link Genre}.
 * </p>
 *
 * <p>В приложении жанры являются предопределенным справочником со следующими значениями:
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
 * <p>Класс наследует базовую функциональность из {@link BaseRepository} и
 * реализует интерфейс {@link GenreStorage}.</p>
 *
 * @see ru.yandex.practicum.filmorate.storage.genres.GenreStorage
 * @see ru.yandex.practicum.filmorate.storage.BaseRepository
 * @see ru.yandex.practicum.filmorate.model.Genre
 */
@Slf4j
@Repository
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

    /** SQL-запрос для получения всех жанров, отсортированных по ID */
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";

    /** SQL-запрос для получения жанра по ID */
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";

    /** SQL-запрос для вставки нового жанра */
    private static final String INSERT_QUERY = "INSERT INTO genres(name) VALUES (?)";

    /** SQL-запрос для обновления существующего жанра */
    private static final String UPDATE_QUERY = "UPDATE genres SET name = ? WHERE genre_id = ?";

    /** SQL-запрос для удаления жанра */
    private static final String DELETE_QUERY = "DELETE FROM genres WHERE genre_id = ?";

    /**
     * Конструктор хранилища жанров.
     *
     * @param jdbc шаблон JDBC для работы с базой данных
     * @param mapper маппер для преобразования результата запроса в объект {@link Genre}
     */
    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Создает новый жанр в базе данных.
     * <p>
     * После вставки записи, полученный идентификатор присваивается объекту жанра.
     * </p>
     *
     * @param genre объект жанра для сохранения
     * @return сохраненный жанр с заполненным идентификатором
     */
    @Override
    public Genre create(Genre genre) {
        int id = (int) insert(INSERT_QUERY, genre.getName());
        genre.setId(id);
        return genre;
    }

    /**
     * Находит жанр по его идентификатору.
     *
     * @param id идентификатор жанра
     * @return {@link Optional} с найденным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не существует
     */
    @Override
    public Optional<Genre> findById(Integer id) {
        return findOne(FIND_BY_ID, id);
    }

    /**
     * Возвращает список всех жанров из базы данных.
     * <p>
     * Результат отсортирован по идентификатору жанра.
     * </p>
     *
     * @return коллекция всех жанров, отсортированная по ID
     */
    @Override
    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    /**
     * Обновляет существующий жанр.
     * <p>
     * Если жанр с указанным ID существует, обновляет его название.
     * Пустое название игнорируется.
     * </p>
     *
     * @param newGenre объект жанра с обновленными данными
     * @return {@link Optional} с обновленным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не найден
     */
    @Override
    public Optional<Genre> update(Genre newGenre) {
        Optional<Genre> oldGenre = findById(newGenre.getId());

        if (oldGenre.isPresent()) {
            Genre updatedGenre = oldGenre.get();
            if (!newGenre.getName().isBlank()) {
                updatedGenre.setName(newGenre.getName());
            }
            update(UPDATE_QUERY, updatedGenre.getName(), updatedGenre.getId());
            return Optional.of(updatedGenre);
        }

        return Optional.empty();
    }

    /**
     * Удаляет жанр по его идентификатору.
     * <p>
     * При удалении жанра также каскадно удаляются связи с фильмами
     * (благодаря ON DELETE CASCADE в схеме БД).
     * </p>
     *
     * @param id идентификатор жанра
     * @return {@link Optional} с удаленным жанром, или пустой {@link Optional},
     *         если жанр с указанным id не найден
     */
    @Override
    public Optional<Genre> removeById(Integer id) {
        Optional<Genre> deletedGenreOptional = findById(id);
        delete(DELETE_QUERY, id);
        return deletedGenreOptional;
    }

    /**
     * Проверяет, существует ли указанный жанр в базе данных.
     * <p>
     * Используется для валидации при добавлении жанров к фильмам.
     * Сравнение происходит по всем полям через {@link Genre#equals(Object)}.
     * </p>
     *
     * @param genre жанр для проверки
     * @return {@code true} если такой жанр существует, иначе {@code false}
     */
    @Override
    public boolean contains(Genre genre) {
        return findAll().stream()
                .anyMatch(genre::equals);
    }
}