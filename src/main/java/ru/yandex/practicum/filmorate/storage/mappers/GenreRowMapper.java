package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Компонент для преобразования строк результата SQL-запроса в объект {@link Genre}.
 * <p>
 * Реализует интерфейс {@link RowMapper} Spring JDBC для автоматического
 * маппинга данных из таблицы {@code genres} в объекты модели Genre.
 * Используется в {@link ru.yandex.practicum.filmorate.storage.genres.GenreDbStorage}
 * при выполнении запросов к базе данных.
 * </p>
 *
 * <p>Маппинг полей:
 * <ul>
 *   <li>{@code genre_id} → {@link Genre#id}</li>
 *   <li>{@code name} → {@link Genre#name}</li>
 * </ul>
 * </p>
 *
 * <p>Также используется в {@link ru.yandex.practicum.filmorate.storage.film.FilmDbStorage}
 * для загрузки жанров, связанных с конкретным фильмом.</p>
 *
 * @see org.springframework.jdbc.core.RowMapper
 * @see ru.yandex.practicum.filmorate.model.Genre
 * @see ru.yandex.practicum.filmorate.storage.genres.GenreDbStorage
 */
@Component
public class GenreRowMapper implements RowMapper<Genre> {

    /**
     * Преобразует текущую строку ResultSet в объект Genre.
     * <p>
     * Извлекает идентификатор и название жанра из соответствующих колонок
     * результата запроса.
     * </p>
     *
     * @param rs объект {@link ResultSet}, содержащий данные текущей строки
     * @param rowNum номер текущей строки (начинается с 0)
     * @return объект {@link Genre}, заполненный данными из ResultSet
     * @throws SQLException если происходит ошибка при доступе к данным ResultSet
     */
    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));

        return genre;
    }
}