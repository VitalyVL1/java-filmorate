package ru.yandex.practicum.filmorate.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

/**
 * Утилитный класс для работы с объектами {@link User}.
 * <p>
 * Содержит вспомогательные методы для манипуляции данными пользователей,
 * которые используются в различных частях приложения (сервисы, хранилища).
 * Все методы класса являются статическими и не требуют создания экземпляра класса.
 * </p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Обновление полей пользователя с логированием изменений</li>
 *   <li>Копирование данных из одного объекта User в другой</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.model.User
 * @see ru.yandex.practicum.filmorate.service.UserService
 * @see ru.yandex.practicum.filmorate.storage.user.UserDbStorage
 * @see ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage
 */
@Slf4j
public class UserUtil {

    /**
     * Обновляет поля существующего пользователя значениями из нового пользователя.
     * <p>
     * Метод проверяет каждое поле нового пользователя на {@code null} и,
     * если поле не null, обновляет соответствующее поле старого пользователя.
     * Все операции обновления логируются с указанием нового значения.
     * </p>
     *
     * <p>Обновляемые поля:
     * <ul>
     *   <li>{@link User#email} — электронная почта</li>
     *   <li>{@link User#login} — логин</li>
     *   <li>{@link User#name} — отображаемое имя</li>
     *   <li>{@link User#birthday} — дата рождения</li>
     * </ul>
     * </p>
     *
     * <p><b>Важно:</b> Метод не обновляет идентификатор пользователя ({@link User#id})
     * и карту друзей ({@link User#friends}). Эти поля должны управляться
     * отдельно на уровне сервисов или хранилищ, так как они не должны
     * изменяться напрямую через обновление профиля.
     * </p>
     *
     * <p>Особенности обновления имени:
     * Если в новом пользователе имя не указано (null), старое имя сохраняется.
     * Обратите внимание, что при создании пользователя в {@link UserService#create(User)}
     * выполняется отдельная логика подстановки логина вместо пустого имени.</p>
     *
     * @param oldUser существующий пользователь, которого нужно обновить
     * @param newUser пользователь с новыми значениями полей (может содержать null-поля)
     * @return обновленный существующий пользователь (тот же объект, что и oldUser)
     * @see ru.yandex.practicum.filmorate.service.UserService#update(User)
     * @see ru.yandex.practicum.filmorate.storage.user.UserDbStorage#update(User)
     * @see ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage#update(User)
     */
    public static User userFieldsUpdate(User oldUser, User newUser) {
        if (newUser.getEmail() != null) {
            log.info("Updating user with email: {}", newUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null) {
            log.info("Updating user with login: {}", newUser.getLogin());
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null) {
            log.info("Updating user with name: {}", newUser.getName());
            oldUser.setName(newUser.getName());
        }
        if (newUser.getBirthday() != null) {
            log.info("Updating user with birthday: {}", newUser.getBirthday());
            oldUser.setBirthday(newUser.getBirthday());
        }
        return oldUser;
    }
}