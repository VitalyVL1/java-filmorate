package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс хранилища для управления пользователями.
 * <p>
 * Определяет контракт для реализации различных способов хранения данных о пользователях
 * (например, в памяти или в базе данных). Помимо стандартных CRUD операций,
 * предоставляет функционал для управления дружескими связями между пользователями.
 * </p>
 *
 * <p>Основные операции:
 * <ul>
 *   <li>CRUD операции с пользователями (создание, чтение, обновление, удаление)</li>
 *   <li>Управление дружескими связями (добавление, удаление, получение списков)</li>
 *   <li>Поиск общих друзей между пользователями</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.storage.user.UserDbStorage
 * @see ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage
 * @see ru.yandex.practicum.filmorate.model.User
 * @see ru.yandex.practicum.filmorate.model.FriendStatus
 */
public interface UserStorage {

    /**
     * Сохраняет нового пользователя в хранилище.
     *
     * @param user объект пользователя для сохранения (без идентификатора)
     * @return сохраненный пользователь с автоматически сгенерированным идентификатором
     */
    User create(User user);

    /**
     * Возвращает пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с найденным пользователем, или пустой {@link Optional},
     *         если пользователь с указанным id не существует
     */
    Optional<User> findById(Long id);

    /**
     * Возвращает список всех пользователей из хранилища.
     *
     * @return коллекция всех пользователей (порядок не гарантирован)
     */
    Collection<User> findAll();

    /**
     * Обновляет существующего пользователя.
     *
     * @param newUser объект пользователя с обновленными данными (должен содержать корректный id)
     * @return {@link Optional} с обновленным пользователем, или пустой {@link Optional},
     *         если пользователь с указанным id не найден
     */
    Optional<User> update(User newUser);

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с удаленным пользователем, или пустой {@link Optional},
     *         если пользователь с указанным id не найден
     */
    Optional<User> removeById(Long id);

    /**
     * Добавляет пользователя в друзья.
     * <p>
     * Создает двунаправленную дружескую связь между двумя пользователями
     * с указанным статусом. Если связь уже существует, может обновлять статус.
     * </p>
     *
     * @param user пользователь, который добавляет в друзья
     * @param friend пользователь, которого добавляют в друзья
     * @param friendStatus статус дружеской связи ({@link FriendStatus#CONFIRMED}
     *                     или {@link FriendStatus#UNCONFIRMED})
     * @return пользователь, который инициировал добавление в друзья
     */
    User addFriend(User user, User friend, FriendStatus friendStatus);

    /**
     * Удаляет пользователя из друзей.
     * <p>
     * Разрывает двунаправленную дружескую связь между двумя пользователями.
     * </p>
     *
     * @param user пользователь, который удаляет из друзей
     * @param friend пользователь, которого удаляют из друзей
     * @return пользователь, который инициировал удаление из друзей
     */
    User removeFriend(User user, User friend);

    /**
     * Возвращает список друзей пользователя.
     * <p>
     * Возвращает всех пользователей, с которыми у указанного пользователя
     * есть дружеская связь (независимо от статуса).
     * </p>
     *
     * @param user пользователь, для которого запрашивается список друзей
     * @return коллекция пользователей, являющихся друзьями указанного пользователя
     */
    Collection<User> findFriends(User user);

    /**
     * Возвращает список общих друзей двух пользователей.
     * <p>
     * Находит пересечение множеств друзей первого и второго пользователя.
     * </p>
     *
     * @param user первый пользователь
     * @param otherUser второй пользователь
     * @return коллекция пользователей, являющихся друзьями обоих указанных пользователей
     */
    Collection<User> findCommonFriends(User user, User otherUser);
}