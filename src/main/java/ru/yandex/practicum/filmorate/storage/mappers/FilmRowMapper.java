package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Компонент для преобразования строк результата SQL-запроса в объект {@link Film}.
 * <p>
 * Реализует интерфейс {@link RowMapper} Spring JDBC для автоматического
 * маппинга данных из базы данных в объекты модели Film. Используется
 * в {@link ru.yandex.practicum.filmorate.storage.film.FilmDbStorage} при
 * выполнении запросов к таблице films.
 * </p>
 *
 * <p>Маппинг полей:
 * <ul>
 *   <li>{@code film_id} → {@link Film#id}</li>
 *   <li>{@code name} → {@link Film#name}</li>
 *   <li>{@code description} → {@link Film#description}</li>
 *   <li>{@code release_date} → {@link Film#releaseDate}</li>
 *   <li>{@code duration} → {@link Film#duration}</li>
 *   <li>{@code mpa_id} → {@link Film#mpa} (только ID, без полного описания)</li>
 * </ul>
 * </p>
 *
 * <p><b>Важно:</b> Данный маппер заполняет только основные поля фильма.
 * Связанные данные (лайки, жанры, полная информация о MPA) загружаются
 * отдельно в {@link ru.yandex.practicum.filmorate.storage.film.FilmDbStorage}.</p>
 *
 * @see org.springframework.jdbc.core.RowMapper
 * @see ru.yandex.practicum.filmorate.model.Film
 * @see ru.yandex.practicum.filmorate.storage.film.FilmDbStorage
 */
@Component
public class FilmRowMapper implements RowMapper<Film> {

    /**
     * Преобразует текущую строку ResultSet в объект Film.
     *
     * @param rs объект {@link ResultSet}, содержащий данные текущей строки
     * @param rowNum номер текущей строки (начинается с 0)
     * @return объект {@link Film}, заполненный данными из ResultSet
     * @throws SQLException если происходит ошибка при доступе к данным ResultSet
     */
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        // Базовые поля фильма
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        // Маппинг ID рейтинга MPA (полная информация загружается отдельно)
        Integer mpaId = rs.getInt("mpa_id");
        if (mpaId != null) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            film.setMpa(mpa);
        }

        return film;
    }
}