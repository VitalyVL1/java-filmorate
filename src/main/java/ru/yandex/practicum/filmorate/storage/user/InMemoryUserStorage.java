package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.UserUtil;

import java.util.*;

/**
 * Реализация хранилища пользователей на основе оперативной памяти (in-memory).
 * <p>
 * Обеспечивает временное хранение данных о пользователях и их дружеских связях,
 * используя {@link HashMap} для хранения объектов. Предназначен для разработки
 * и тестирования, не обеспечивает персистентность данных между перезапусками
 * приложения.
 * </p>
 *
 * <p>Управление дружескими связями:
 * <ul>
 *   <li>Дружеские связи хранятся внутри объектов {@link User} в виде карты
 *       {@code Map<Long, FriendStatus>}, где ключ - ID друга, значение - статус</li>
 *   <li>При подтвержденной дружбе ({@link FriendStatus#CONFIRMED}) связь становится
 *       двунаправленной и добавляется в карты обоих пользователей</li>
 *   <li>Поиск друзей и общих друзей выполняется через потоковые операции над этими картами</li>
 * </ul>
 * </p>
 *
 * <p><b>Важно:</b> Данная реализация не подходит для production-среды,
 * так как все данные теряются при остановке приложения. Используется
 * на этапе разработки для быстрого прототипирования.</p>
 *
 * @see ru.yandex.practicum.filmorate.storage.user.UserStorage
 * @see ru.yandex.practicum.filmorate.storage.user.UserDbStorage
 * @see ru.yandex.practicum.filmorate.model.User
 */
@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    /**
     * Хранилище пользователей в памяти.
     * Ключ - идентификатор пользователя, значение - объект пользователя.
     */
    private final Map<Long, User> users = new HashMap<>();

    /**
     * Создает нового пользователя в in-memory хранилище.
     * <p>
     * Генерирует новый уникальный идентификатор для пользователя.
     * </p>
     *
     * @param user объект пользователя для сохранения
     * @return сохраненный пользователь с заполненным идентификатором
     */
    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    /**
     * Находит пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с найденным пользователем, или пустой {@link Optional},
     *         если пользователь с указанным id не существует
     */
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return коллекция всех пользователей в памяти
     */
    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    /**
     * Обновляет существующего пользователя.
     * <p>
     * Использует {@link UserUtil#userFieldsUpdate} для копирования обновленных полей.
     * </p>
     *
     * @param newUser объект пользователя с обновленными данными
     * @return {@link Optional} с обновленным пользователем, или пустой {@link Optional},
     *         если пользователь с указанным id не найден
     */
    @Override
    public Optional<User> update(User newUser) {
        if (!users.containsKey(newUser.getId())) {
            return Optional.empty();
        }

        User updatedUser = UserUtil.userFieldsUpdate(users.get(newUser.getId()), newUser);
        users.put(updatedUser.getId(), updatedUser);

        return Optional.of(users.get(newUser.getId()));
    }

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return {@link Optional} с удаленным пользователем, или пустой {@link Optional},
     *         если пользователь с указанным id не найден
     */
    @Override
    public Optional<User> removeById(Long id) {
        return Optional.ofNullable(users.remove(id));
    }

    /**
     * Добавляет пользователя в друзья.
     * <p>
     * Обновляет карты друзей в памяти пользователей. Если статус
     * {@link FriendStatus#CONFIRMED}, создает двунаправленную связь,
     * добавляя запись в карты обоих пользователей.
     * </p>
     *
     * @param user пользователь, который добавляет в друзья
     * @param friend пользователь, которого добавляют в друзья
     * @param friendStatus статус дружеской связи
     * @return пользователь, инициировавший добавление
     */
    @Override
    public User addFriend(User user, User friend, FriendStatus friendStatus) {
        user.getFriends().put(friend.getId(), friendStatus);

        if (friendStatus.equals(FriendStatus.CONFIRMED)) {
            friend.getFriends().put(user.getId(), FriendStatus.CONFIRMED);
        }

        return user;
    }

    /**
     * Удаляет пользователя из друзей.
     * <p>
     * Удаляет запись из карты друзей указанного пользователя.
     * Вторую сторону не обновляет, так как при подтвержденной дружбе
     * связь была двусторонней и должна быть удалена отдельно (или
     * считается, что при удалении одной стороны связь разрывается).
     * </p>
     *
     * @param user пользователь, который удаляет из друзей
     * @param friend пользователь, которого удаляют из друзей
     * @return пользователь, инициировавший удаление
     */
    @Override
    public User removeFriend(User user, User friend) {
        user.getFriends().remove(friend.getId());
        return user;
    }

    /**
     * Возвращает список друзей пользователя.
     * <p>
     * Получает множество ID друзей из карты пользователя и преобразует
     * каждый ID в соответствующий объект {@link User} из общего хранилища.
     * </p>
     *
     * @param user пользователь, для которого запрашивается список друзей
     * @return коллекция объектов User, являющихся друзьями указанного пользователя
     */
    @Override
    public Collection<User> findFriends(User user) {
        return user.getFriends()
                .keySet()
                .stream()
                .map(this.users::get)
                .toList();
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     * <p>
     * Находит пересечение множеств ID друзей первого и второго пользователя,
     * затем преобразует найденные ID в объекты {@link User} из общего хранилища.
     * </p>
     *
     * @param user первый пользователь
     * @param otherUser второй пользователь
     * @return коллекция объектов User, являющихся друзьями обоих пользователей
     */
    @Override
    public Collection<User> findCommonFriends(User user, User otherUser) {
        Set<Long> userFriends = user.getFriends().keySet();
        Set<Long> otherUserFriends = otherUser.getFriends().keySet();

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(this.users::get)
                .toList();
    }

    /**
     * Генерирует следующий уникальный идентификатор для нового пользователя.
     * <p>
     * Находит максимальный существующий ID в хранилище и увеличивает его на 1.
     * Если хранилище пусто, возвращает 1.
     * </p>
     *
     * @return следующий уникальный идентификатор
     */
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .reduce(Long::max)
                .orElse(0L);
        return ++currentMaxId;
    }
}