package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.util.UserUtil;

import java.sql.Date;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация хранилища пользователей на основе базы данных (H2).
 * <p>
 * Обеспечивает хранение (persistence) данных о пользователях и их дружеских связях
 * в реляционной базе данных. Использует {@link JdbcTemplate} для выполнения
 * SQL-запросов и {@link RowMapper} для преобразования результатов запросов
 * в объекты {@link User}.
 * </p>
 *
 * <p>Основные особенности:
 * <ul>
 *   <li>Автоматическое управление двунаправленными дружескими связями</li>
 *   <li>Поддержка статусов дружбы {@link FriendStatus}</li>
 *   <li>Полная загрузка информации о друзьях при каждом запросе пользователя</li>
 *   <li>Каскадное удаление связанных данных (дружба, лайки) при удалении пользователя</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.storage.user.UserStorage
 * @see ru.yandex.practicum.filmorate.storage.BaseRepository
 * @see ru.yandex.practicum.filmorate.model.User
 */
@Slf4j
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    /** SQL-запрос для получения всех пользователей */
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";

    /** SQL-запрос для получения пользователя по ID */
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";

    /** SQL-запрос для вставки нового пользователя */
    private static final String INSERT_USER_QUERY = "INSERT INTO users(email, login, name, birthday) " +
                                                    "VALUES (?, ?, ?, ?)";

    /** SQL-запрос для обновления существующего пользователя */
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                                                    "WHERE user_id = ?";

    /** SQL-запрос для удаления пользователя */
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";

    /** SQL-запрос для добавления дружеской связи */
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id, status) " +
                                                      "VALUES (?, ?, ?)";

    /** SQL-запрос для удаления дружеской связи */
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends " +
                                                      "WHERE user_id = ? AND friend_id = ?";

    /** SQL-запрос для получения друзей пользователя */
    private static final String FIND_FRIENDS_BY_USER_QUERY = "SELECT * FROM users " +
                                                             "WHERE user_id IN (" +
                                                             "SELECT friend_id FROM friends " +
                                                             "WHERE user_id = ?" +
                                                             ")";

    /** SQL-запрос для получения общих друзей двух пользователей (через INTERSECT) */
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT * FROM users " +
                                                            "WHERE user_id IN (" +
                                                            "SELECT friend_id FROM friends WHERE user_id = ?" +
                                                            "INTERSECT " +
                                                            "SELECT friend_id FROM friends WHERE user_id = ?" +
                                                            ")";

    /** SQL-запрос для получения карты друзей со статусами для конкретного пользователя */
    private static final String FOR_MAP_FRIENDS_ID_STATUS = "SELECT friend_id, status FROM friends " +
                                                            "WHERE user_id = ?";

    /**
     * Конструктор хранилища пользователей.
     *
     * @param jdbc шаблон JDBC для работы с базой данных
     * @param mapper маппер для преобразования результата запроса в объект {@link User}
     */
    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Создает нового пользователя в базе данных.
     *
     * @param user объект пользователя для сохранения
     * @return сохраненный пользователь с заполненным идентификатором
     */
    @Override
    public User create(User user) {
        long id = insert(
                INSERT_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    /**
     * Находит пользователя по его идентификатору.
     * <p>
     * При загрузке пользователя также загружает информацию о его друзьях
     * (карту friendId → FriendStatus).
     * </p>
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с пользователем и его друзьями, или пустой {@link Optional},
     *         если пользователь не найден
     */
    @Override
    public Optional<User> findById(Long id) {
        Optional<User> user = findOne(FIND_USER_BY_ID_QUERY, id);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        user.get().getFriends().putAll(getFriends(id));

        return user;
    }

    /**
     * Возвращает список всех пользователей.
     * <p>
     * Для каждого пользователя загружает информацию о его друзьях.
     * </p>
     *
     * @return коллекция всех пользователей с информацией о друзьях
     */
    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_USERS_QUERY)
                .stream()
                .map(user -> {
                    user.getFriends().putAll(getFriends(user.getId()));
                    return user;
                })
                .toList();
    }

    /**
     * Обновляет существующего пользователя.
     * <p>
     * Использует {@link UserUtil#userFieldsUpdate} для копирования обновленных полей.
     * </p>
     *
     * @param newUser объект пользователя с обновленными данными
     * @return {@link Optional} с обновленным пользователем, или пустой {@link Optional},
     *         если пользователь не найден
     */
    @Override
    public Optional<User> update(User newUser) {
        Optional<User> oldUserOptional = findById(newUser.getId());

        if (oldUserOptional.isPresent()) {
            User updatedUser = UserUtil.userFieldsUpdate(oldUserOptional.get(), newUser);

            update(
                    UPDATE_USER_QUERY,
                    updatedUser.getEmail(),
                    updatedUser.getLogin(),
                    updatedUser.getName(),
                    Date.valueOf(updatedUser.getBirthday()),
                    updatedUser.getId()
            );

            return Optional.of(updatedUser);
        }

        return Optional.empty();
    }

    /**
     * Удаляет пользователя по его идентификатору.
     * <p>
     * При удалении пользователя каскадно удаляются связанные записи:
     * дружеские связи и лайки (благодаря ON DELETE CASCADE в схеме БД).
     * </p>
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с удаленным пользователем, или пустой {@link Optional},
     *         если пользователь не найден
     */
    @Override
    public Optional<User> removeById(Long id) {
        Optional<User> deletedUserOptional = findById(id);
        delete(DELETE_USER_QUERY, id);
        return deletedUserOptional;
    }

    /**
     * Добавляет пользователя в друзья.
     * <p>
     * Создает дружескую связь. Если статус {@link FriendStatus#CONFIRMED},
     * создает двунаправленную связь (обе стороны). Если статус
     * {@link FriendStatus#UNCONFIRMED}, создает только одностороннюю связь.
     * </p>
     *
     * @param user пользователь, который добавляет в друзья
     * @param friend пользователь, которого добавляют в друзья
     * @param friendStatus статус дружеской связи
     * @return пользователь, инициировавший добавление
     */
    @Override
    public User addFriend(User user, User friend, FriendStatus friendStatus) {
        jdbc.update(
                INSERT_FRIEND_QUERY,
                user.getId(),
                friend.getId(),
                friendStatus.toString()
        );

        user.getFriends().put(friend.getId(), friendStatus);

        if (friendStatus.equals(FriendStatus.CONFIRMED)) {
            jdbc.update(
                    INSERT_FRIEND_QUERY,
                    friend.getId(),
                    user.getId(),
                    friendStatus.toString()
            );

            friend.getFriends().put(user.getId(), friendStatus);
        }

        return user;
    }

    /**
     * Удаляет пользователя из друзей.
     * <p>
     * Удаляет дружескую связь. Вторую сторону не обновляет (связь была односторонней
     * или удаляется автоматически каскадно).
     * </p>
     *
     * @param user пользователь, который удаляет из друзей
     * @param friend пользователь, которого удаляют из друзей
     * @return пользователь, инициировавший удаление
     */
    @Override
    public User removeFriend(User user, User friend) {
        delete(DELETE_FRIEND_QUERY, user.getId(), friend.getId());
        user.getFriends().remove(friend.getId());
        return user;
    }

    /**
     * Возвращает список друзей пользователя.
     *
     * @param user пользователь, для которого запрашивается список друзей
     * @return коллекция пользователей, являющихся друзьями указанного пользователя
     */
    @Override
    public Collection<User> findFriends(User user) {
        return findMany(FIND_FRIENDS_BY_USER_QUERY, user.getId());
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     * <p>
     * Использует SQL оператор INTERSECT для нахождения пересечения множеств друзей.
     * Для каждого найденного общего друга загружает информацию о его друзьях.
     * </p>
     *
     * @param user первый пользователь
     * @param otherUser второй пользователь
     * @return коллекция пользователей, являющихся друзьями обоих пользователей
     */
    @Override
    public Collection<User> findCommonFriends(User user, User otherUser) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, user.getId(), otherUser.getId()).stream()
                .map(usr -> {
                    usr.getFriends().putAll(getFriends(usr.getId()));
                    return usr;
                })
                .toList();
    }

    /**
     * Получает карту друзей пользователя со статусами.
     * <p>
     * Преобразует результат SQL-запроса в Map, где ключ - ID друга,
     * значение - статус дружбы {@link FriendStatus}.
     * </p>
     *
     * @param userId идентификатор пользователя
     * @return карта, где ключ - ID друга, значение - статус дружбы
     */
    private Map<Long, FriendStatus> getFriends(Long userId) {
        return jdbc.queryForList(FOR_MAP_FRIENDS_ID_STATUS, userId)
                .stream()
                .collect(Collectors.toMap(
                        k -> ((Number) k.get("friend_id")).longValue(), // двойной кастинг для преобразования в Long
                        v -> FriendStatus.valueOf((String) v.get("status")))
                );
    }
}