package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Базовый абстрактный класс для всех JDBC-репозиториев приложения.
 * <p>
 * Предоставляет общие методы для работы с базой данных, инкапсулируя типовые операции:
 * выполнение запросов, обработку результатов, генерацию ключей и обработку исключений.
 * Все конкретные реализации хранилищ (FilmDbStorage, UserDbStorage, GenreDbStorage, MpaDbStorage)
 * наследуются от этого класса.
 * </p>
 *
 * <p>Основные возможности:
 * <ul>
 *   <li>Выполнение запросов с автоматическим маппингом результатов через {@link RowMapper}</li>
 *   <li>Безопасная обработка {@link EmptyResultDataAccessException} через {@link Optional}</li>
 *   <li>Поддержка автоматической генерации идентификаторов при вставке</li>
 *   <li>Единообразная обработка ошибок с преобразованием в {@link InternalServerException}</li>
 * </ul>
 * </p>
 *
 * @param <T> тип сущности, с которой работает репозиторий (Film, User, Genre, Mpa)
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.core.RowMapper
 */
@RequiredArgsConstructor
public class BaseRepository<T> {

    /** Шаблон JDBC для выполнения SQL-запросов */
    protected final JdbcTemplate jdbc;

    /** Маппер для преобразования строк ResultSet в объекты типа T */
    protected final RowMapper<T> mapper;

    /**
     * Выполняет запрос, ожидающий получения одной записи.
     * <p>
     * Если запрос возвращает ровно одну строку, она преобразуется в объект T.
     * Если запрос не возвращает результатов, метод возвращает пустой {@link Optional}.
     * Любые другие ошибки (например, неоднозначность результата) пробрасываются выше.
     * </p>
     *
     * @param query SQL-запрос
     * @param params параметры запроса (подставляются на место знаков ?)
     * @return {@link Optional} с результатом запроса, или пустой {@link Optional},
     *         если запрос не вернул результатов
     */
    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Выполняет запрос, ожидающий получения нескольких записей.
     * <p>
     * Если запрос возвращает результаты, каждая строка преобразуется в объект T.
     * Если запрос не возвращает результатов, возвращается пустой список.
     * </p>
     *
     * @param query SQL-запрос
     * @param params параметры запроса (подставляются на место знаков ?)
     * @return список объектов T (может быть пустым)
     */
    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    /**
     * Выполняет запрос на удаление данных.
     *
     * @param query SQL-запрос DELETE
     * @param params параметры запроса
     * @return {@code true} если была удалена хотя бы одна запись, иначе {@code false}
     */
    protected boolean delete(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        return rowsDeleted > 0;
    }

    /**
     * Выполняет запрос на обновление данных.
     * <p>
     * Если ни одна запись не была обновлена, выбрасывает {@link InternalServerException}.
     * </p>
     *
     * @param query SQL-запрос UPDATE
     * @param params параметры запроса
     * @throws InternalServerException если ни одна запись не была обновлена
     */
    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    /**
     * Выполняет запрос на вставку данных с автоматической генерацией ключа.
     * <p>
     * После выполнения вставки извлекает сгенерированный ключ (обычно автоинкрементный ID)
     * и возвращает его как long. Если ключ не был сгенерирован, выбрасывает
     * {@link InternalServerException}.
     * </p>
     *
     * @param query SQL-запрос INSERT
     * @param params параметры запроса
     * @return сгенерированный идентификатор новой записи
     * @throws InternalServerException если не удалось получить сгенерированный ключ
     */
    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        // Преобразование сгенерированного ключа в long
        // Если ключ не сгенерирован, возникает NullPointerException, который преобразуется
        // в InternalServerException для единообразной обработки ошибок
        try {
            return keyHolder.getKey().longValue();
        } catch (NullPointerException ignored) {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }
}