package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Компонент для преобразования строк результата SQL-запроса в объект {@link Mpa}.
 * <p>
 * Реализует интерфейс {@link RowMapper} Spring JDBC для автоматического
 * маппинга данных из таблицы {@code mpa} в объекты модели Mpa.
 * Используется в {@link ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage}
 * и {@link ru.yandex.practicum.filmorate.storage.film.FilmDbStorage} для получения
 * полной информации о возрастных рейтингах.
 * </p>
 *
 * <p>Маппинг полей:
 * <ul>
 *   <li>{@code mpa_id} → {@link Mpa#id}</li>
 *   <li>{@code name} → {@link Mpa#name}</li>
 *   <li>{@code description} → {@link Mpa#description}</li>
 * </ul>
 * </p>
 *
 * <p>Заполняет все поля объекта Mpa, предоставляя полную информацию о рейтинге:
 * идентификатор, кодовое обозначение (G, PG, PG-13, R, NC-17) и развернутое описание.</p>
 *
 * @see org.springframework.jdbc.core.RowMapper
 * @see ru.yandex.practicum.filmorate.model.Mpa
 * @see ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage
 * @see ru.yandex.practicum.filmorate.storage.film.FilmDbStorage
 */
@Component
public class MpaRowMapper implements RowMapper<Mpa> {

    /**
     * Преобразует текущую строку ResultSet в объект Mpa.
     * <p>
     * Извлекает идентификатор, название и описание рейтинга из соответствующих
     * колонок результата запроса, создавая полностью заполненный объект Mpa.
     * </p>
     *
     * @param rs объект {@link ResultSet}, содержащий данные текущей строки
     * @param rowNum номер текущей строки (начинается с 0)
     * @return объект {@link Mpa}, заполненный всеми полями из ResultSet
     * @throws SQLException если происходит ошибка при доступе к данным ResultSet
     */
    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("name"));
        mpa.setDescription(rs.getString("description"));

        return mpa;
    }
}