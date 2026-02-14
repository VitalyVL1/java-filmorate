package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * Реализация хранилища рейтингов MPA на основе базы данных (H2).
 * <p>
 * Обеспечивает хранение (persistence) данных о возрастных рейтингах MPA
 * (Motion Picture Association) в реляционной базе данных. Использует
 * {@link JdbcTemplate} для выполнения SQL-запросов и {@link RowMapper}
 * для преобразования результатов запросов в объекты {@link Mpa}.
 * </p>
 *
 * <p>Стандартные рейтинги MPA, предустановленные в базе:
 * <ul>
 *   <li>1 - G (General Audiences) — нет возрастных ограничений</li>
 *   <li>2 - PG (Parental Guidance Suggested) — рекомендуется присутствие родителей</li>
 *   <li>3 - PG-13 (Parents Strongly Cautioned) — детям до 13 лет не рекомендуется</li>
 *   <li>4 - R (Restricted) — лицам до 17 лет обязательно присутствие родителей</li>
 *   <li>5 - NC-17 (Adults Only) — только для взрослых (18+)</li>
 * </ul>
 * </p>
 *
 * <p>Класс наследует базовую функциональность из {@link BaseRepository} и
 * реализует интерфейс {@link MpaStorage}.</p>
 *
 * @see ru.yandex.practicum.filmorate.storage.mpa.MpaStorage
 * @see ru.yandex.practicum.filmorate.storage.BaseRepository
 * @see ru.yandex.practicum.filmorate.model.Mpa
 */
@Slf4j
@Repository
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {

    /** SQL-запрос для получения всех рейтингов MPA, отсортированных по ID */
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY mpa_id";

    /** SQL-запрос для получения рейтинга MPA по ID */
    private static final String FIND_BY_ID = "SELECT * FROM mpa WHERE mpa_id = ?";

    /** SQL-запрос для вставки нового рейтинга MPA */
    private static final String INSERT_QUERY = "INSERT INTO mpa(name, description) VALUES (?, ?)";

    /** SQL-запрос для обновления существующего рейтинга MPA */
    private static final String UPDATE_QUERY = "UPDATE mpa SET name = ?, description = ? WHERE mpa_id = ?";

    /** SQL-запрос для удаления рейтинга MPA */
    private static final String DELETE_QUERY = "DELETE FROM mpa WHERE mpa_id = ?";

    /**
     * Конструктор хранилища рейтингов MPA.
     *
     * @param jdbc шаблон JDBC для работы с базой данных
     * @param mapper маппер для преобразования результата запроса в объект {@link Mpa}
     */
    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Создает новый рейтинг MPA в базе данных.
     * <p>
     * После вставки записи, полученный идентификатор присваивается объекту рейтинга.
     * </p>
     *
     * @param mpa объект рейтинга для сохранения
     * @return сохраненный рейтинг с заполненным идентификатором
     */
    @Override
    public Mpa create(Mpa mpa) {
        int id = (int) insert(INSERT_QUERY, mpa.getName(), mpa.getDescription());
        mpa.setId(id);
        return mpa;
    }

    /**
     * Находит рейтинг MPA по его идентификатору.
     *
     * @param id идентификатор рейтинга (для стандартных значений от 1 до 5)
     * @return {@link Optional} с найденным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не существует
     */
    @Override
    public Optional<Mpa> findById(Integer id) {
        return findOne(FIND_BY_ID, id);
    }

    /**
     * Возвращает список всех рейтингов MPA из базы данных.
     * <p>
     * Результат отсортирован по идентификатору рейтинга.
     * </p>
     *
     * @return коллекция всех рейтингов, отсортированная по ID
     */
    @Override
    public Collection<Mpa> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    /**
     * Обновляет существующий рейтинг MPA.
     * <p>
     * Если рейтинг с указанным ID существует, обновляет его название и описание.
     * Пустые строки для названия и описания игнорируются (обновление не производится).
     * </p>
     *
     * @param newMpa объект рейтинга с обновленными данными
     * @return {@link Optional} с обновленным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не найден
     */
    @Override
    public Optional<Mpa> update(Mpa newMpa) {
        Optional<Mpa> oldMpa = findById(newMpa.getId());

        if (oldMpa.isPresent()) {
            Mpa updatedMpa = oldMpa.get();
            if (!newMpa.getName().isBlank()) {
                updatedMpa.setName(newMpa.getName());
            }
            if (!newMpa.getDescription().isBlank()) {
                updatedMpa.setDescription(newMpa.getDescription());
            }
            update(UPDATE_QUERY, updatedMpa.getName(), updatedMpa.getDescription(), updatedMpa.getId());
            return Optional.of(updatedMpa);
        }

        return Optional.empty();
    }

    /**
     * Удаляет рейтинг MPA по его идентификатору.
     * <p>
     * При удалении рейтинга в таблице {@code films} значение {@code mpa_id}
     * устанавливается в NULL (благодаря ON DELETE SET NULL в схеме БД).
     * </p>
     *
     * @param id идентификатор рейтинга
     * @return {@link Optional} с удаленным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не найден
     */
    @Override
    public Optional<Mpa> removeById(Integer id) {
        Optional<Mpa> deletedMpaOptional = findById(id);
        delete(DELETE_QUERY, id);
        return deletedMpaOptional;
    }

    /**
     * Проверяет, существует ли указанный рейтинг MPA в базе данных.
     * <p>
     * Используется для валидации при добавлении рейтингов к фильмам.
     * Сравнение происходит по всем полям через {@link Mpa#equals(Object)}.
     * </p>
     *
     * @param mpa рейтинг для проверки
     * @return {@code true} если такой рейтинг существует, иначе {@code false}
     */
    @Override
    public boolean contains(Mpa mpa) {
        return findAll().stream()
                .anyMatch(mpa::equals);
    }
}