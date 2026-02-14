package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Arrays;
import java.util.Collection;

/**
 * Сервисный слой для управления пользователями.
 * <p>
 * Реализует бизнес-логику работы с пользователями, включая операции создания,
 * обновления, удаления, поиска, а также функционал управления дружескими связями
 * (добавление в друзья, удаление из друзей, просмотр списка друзей и общих друзей).
 * </p>
 *
 * <p>Основные бизнес-правила:
 * <ul>
 *   <li>Email должен быть уникальным в системе</li>
 *   <li>Если имя пользователя не указано, оно заменяется на логин</li>
 *   <li>Нельзя добавить в друзья самого себя (проверка на уровне хранилища)</li>
 *   <li>Статус дружбы должен быть одним из: {@link FriendStatus#UNCONFIRMED}, {@link FriendStatus#CONFIRMED}</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.model.User
 * @see ru.yandex.practicum.filmorate.model.FriendStatus
 * @see ru.yandex.practicum.filmorate.storage.user.UserStorage
 */
@Service
public class UserService {
    private final UserStorage userStorage;

    /**
     * Конструктор сервиса пользователей.
     *
     * @param userStorage хранилище пользователей (алиас из {@link ru.yandex.practicum.filmorate.config.AppConfig})
     */
    public UserService(@Qualifier("userStorageAlias") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Создает нового пользователя.
     * <p>
     * Перед созданием проверяет уникальность email. Если имя пользователя не указано
     * (null или пустая строка), автоматически устанавливает имя равное логину.
     * </p>
     *
     * @param user объект пользователя для создания
     * @return созданный пользователь с автоматически сгенерированным идентификатором
     * @throws DuplicatedDataException если пользователь с таким email уже существует
     */
    public User create(User user) {
        if (containsEmail(user)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin()); // копируем значение login в name если name не задан
        }

        return userStorage.create(user);
    }

    /**
     * Находит пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь
     * @throws ConditionsNotMetException если id равен null
     * @throws NotFoundException если пользователь с указанным id не найден
     */
    public User findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return коллекция всех пользователей в системе
     */
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    /**
     * Обновляет существующего пользователя.
     * <p>
     * Проверяет существование пользователя и уникальность email при его изменении.
     * </p>
     *
     * @param newUser объект пользователя с обновленными данными
     * @return обновленный пользователь
     * @throws ConditionsNotMetException если id не указан
     * @throws NotFoundException если пользователь с указанным id не найден
     * @throws DuplicatedDataException если новый email уже используется другим пользователем
     */
    public User update(User newUser) {
        User oldUser = findById(newUser.getId());

        if (newUser.getEmail() != null &&
            !newUser.getEmail().equals(oldUser.getEmail()) &&
            containsEmail(newUser)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        return userStorage.update(newUser)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден")
                );
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return удаленный пользователь
     * @throws ConditionsNotMetException если id равен null
     * @throws NotFoundException если пользователь с указанным id не найден
     */
    public User removeById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return userStorage.removeById(id)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + id + " не найден")
                );
    }

    /**
     * Добавляет пользователя в друзья.
     * <p>
     * Создает дружескую связь между двумя пользователями с указанным статусом.
     * Проверяет существование обоих пользователей.
     * </p>
     *
     * @param id идентификатор пользователя, который добавляет в друзья
     * @param friendId идентификатор пользователя, которого добавляют в друзья
     * @param status статус дружбы (строковое представление {@link FriendStatus})
     * @return пользователь, который инициировал добавление в друзья
     * @throws ConditionsNotMetException если указан некорректный статус
     * @throws NotFoundException если один из пользователей не найден
     */
    public User addFriend(Long id, Long friendId, String status) {
        return userStorage.addFriend(
                findById(id),
                findById(friendId),
                checkStatus(status)
        );
    }

    /**
     * Удаляет пользователя из друзей.
     * <p>
     * Разрывает дружескую связь между двумя пользователями.
     * Проверяет существование обоих пользователей.
     * </p>
     *
     * @param id идентификатор пользователя, который удаляет из друзей
     * @param friendId идентификатор пользователя, которого удаляют из друзей
     * @return пользователь, который инициировал удаление из друзей
     * @throws NotFoundException если один из пользователей или дружеская связь не найдены
     */
    public User removeFriend(Long id, Long friendId) {
        return userStorage.removeFriend(findById(id), findById(friendId));
    }

    /**
     * Возвращает список друзей пользователя.
     *
     * @param id идентификатор пользователя
     * @return коллекция пользователей, являющихся друзьями указанного пользователя
     * @throws NotFoundException если пользователь с указанным id не найден
     */
    public Collection<User> findFriends(Long id) {
        return userStorage.findFriends(findById(id));
    }

    /**
     * Возвращает список общих друзей двух пользователей.
     *
     * @param id идентификатор первого пользователя
     * @param otherId идентификатор второго пользователя
     * @return коллекция пользователей, являющихся друзьями обоих указанных пользователей
     * @throws NotFoundException если один из пользователей не найден
     */
    public Collection<User> findCommonFriends(Long id, Long otherId) {
        return userStorage.findCommonFriends(findById(id), findById(otherId));
    }

    /**
     * Проверяет, существует ли пользователь с таким email.
     * <p>
     * Вспомогательный метод для проверки уникальности email при создании и обновлении.
     * </p>
     *
     * @param user пользователь с проверяемым email
     * @return {@code true} если пользователь с таким email уже существует, иначе {@code false}
     */
    private boolean containsEmail(User user) {
        return userStorage.findAll().stream()
                .map(User::getEmail)
                .anyMatch(user.getEmail()::equals);
    }

    /**
     * Преобразует строковое представление статуса в перечисление {@link FriendStatus}.
     * <p>
     * Выполняет валидацию входного параметра статуса. Регистр символов не учитывается.
     * </p>
     *
     * @param status строковое представление статуса ("CONFIRMED" или "UNCONFIRMED")
     * @return соответствующий элемент перечисления {@link FriendStatus}
     * @throws ConditionsNotMetException если строка не соответствует ни одному статусу
     */
    private FriendStatus checkStatus(String status) {
        try {
            return FriendStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConditionsNotMetException("Статус должен быть: " + Arrays.toString(FriendStatus.values()));
        }
    }
}