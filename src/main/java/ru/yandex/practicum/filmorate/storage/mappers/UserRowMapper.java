package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Компонент для преобразования строк результата SQL-запроса в объект {@link User}.
 * <p>
 * Реализует интерфейс {@link RowMapper} Spring JDBC для автоматического
 * маппинга данных из таблицы {@code users} в объекты модели User.
 * Используется в {@link ru.yandex.practicum.filmorate.storage.user.UserDbStorage}
 * при выполнении запросов к базе данных.
 * </p>
 *
 * <p>Маппинг полей:
 * <ul>
 *   <li>{@code user_id} → {@link User#id}</li>
 *   <li>{@code email} → {@link User#email}</li>
 *   <li>{@code login} → {@link User#login}</li>
 *   <li>{@code name} → {@link User#name}</li>
 *   <li>{@code birthday} → {@link User#birthday}</li>
 * </ul>
 * </p>
 *
 * <p><b>Важно:</b> Данный маппер заполняет только основные поля пользователя.
 * Информация о друзьях ({@link User#friends}) загружается отдельно
 * в {@link ru.yandex.practicum.filmorate.storage.user.UserDbStorage}.</p>
 *
 * @see org.springframework.jdbc.core.RowMapper
 * @see ru.yandex.practicum.filmorate.model.User
 * @see ru.yandex.practicum.filmorate.storage.user.UserDbStorage
 */
@Component
public class UserRowMapper implements RowMapper<User> {

    /**
     * Преобразует текущую строку ResultSet в объект User.
     * <p>
     * Извлекает все основные поля пользователя из соответствующих колонок
     * результата запроса: идентификатор, email, логин, имя и дату рождения.
     * </p>
     *
     * @param rs объект {@link ResultSet}, содержащий данные текущей строки
     * @param rowNum номер текущей строки (начинается с 0)
     * @return объект {@link User}, заполненный основными полями из ResultSet
     * @throws SQLException если происходит ошибка при доступе к данным ResultSet
     */
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        return user;
    }
}