package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

/**
 * REST-контроллер для управления пользователями.
 * <p>
 * Предоставляет HTTP-эндпоинты для выполнения операций CRUD с пользователями,
 * а также для управления дружескими связями между ними. Поддерживает функционал
 * добавления в друзья, удаления из друзей, просмотра списка друзей и поиска
 * общих друзей. Все методы используют сервисный слой {@link UserService}.
 * </p>
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    /**
     * Получает список всех пользователей.
     *
     * @return коллекция всех пользователей, зарегистрированных в системе
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> findAll() {
        log.debug("Find all users");
        return userService.findAll();
    }

    /**
     * Получает пользователя по его уникальному идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь с указанным идентификатором
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь с указанным id не найден
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User findById(@PathVariable Long id) {
        log.debug("Find user by id {}", id);
        return userService.findById(id);
    }

    /**
     * Создает нового пользователя.
     * <p>
     * При создании пользователь проходит валидацию: email должен быть корректным и уникальным,
     * логин не может содержать пробелы, дата рождения не может быть в будущем.
     * Если имя пользователя не указано (пустое), оно заменяется на логин.
     * </p>
     *
     * @param user объект пользователя для создания (должен быть валидным)
     * @return созданный пользователь с автоматически сгенерированным идентификатором
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        log.debug("Create user: {}", user);
        return userService.create(user);
    }

    /**
     * Обновляет существующего пользователя.
     * <p>
     * Пользователь обновляется по его идентификатору, который должен быть указан в теле запроса.
     * Все поля пользователя, кроме идентификатора, могут быть изменены.
     * Валидация применяется так же, как и при создании.
     * </p>
     *
     * @param newUser объект пользователя с обновленными данными (должен содержать корректный id)
     * @return обновленный пользователь
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь с указанным id не найден
     */
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User update(@RequestBody User newUser) {
        log.debug("Update user: {}", newUser);
        return userService.update(newUser);
    }

    /**
     * Добавляет пользователя в друзья.
     * <p>
     * Создает дружескую связь между двумя пользователями. Статус дружбы может быть
     * указан как параметр запроса (по умолчанию {@code UNCONFIRMED}). При добавлении
     * создается двунаправленная связь с соответствующим статусом.
     * </p>
     *
     * @param id идентификатор пользователя, который добавляет в друзья
     * @param friendId идентификатор пользователя, которого добавляют в друзья
     * @param status статус дружбы: {@code UNCONFIRMED} (неподтвержденная) или {@code CONFIRMED} (подтвержденная)
     * @return пользователь, который инициировал добавление в друзья
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если один из пользователей не найден
     */
    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public User addFriend(
            @PathVariable Long id,
            @PathVariable Long friendId,
            @RequestParam(value = "status", required = false, defaultValue = "UNCONFIRMED") String status
    ) {
        log.debug("Add friend {} to user: {}, status {}", friendId, id, status);
        return userService.addFriend(id, friendId, status);
    }

    /**
     * Удаляет пользователя из друзей.
     * <p>
     * Разрывает дружескую связь между двумя пользователями. Удаляются соответствующие
     * записи в обе стороны.
     * </p>
     *
     * @param id идентификатор пользователя, который удаляет из друзей
     * @param friendId идентификатор пользователя, которого удаляют из друзей
     * @return пользователь, который инициировал удаление из друзей
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если один из пользователей
     *         или дружеская связь не найдены
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.debug("Delete friend {} from user: {}", friendId, id);
        return userService.removeFriend(id, friendId);
    }

    /**
     * Получает список друзей пользователя.
     *
     * @param id идентификатор пользователя
     * @return коллекция пользователей, являющихся друзьями указанного пользователя
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если пользователь с указанным id не найден
     */
    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> findFriends(@PathVariable Long id) {
        log.debug("Find friends {}", id);
        return userService.findFriends(id);
    }

    /**
     * Получает список общих друзей двух пользователей.
     * <p>
     * Возвращает пересечение множеств друзей первого и второго пользователя.
     * Полезно для поиска общих знакомых в социальной сети.
     * </p>
     *
     * @param id идентификатор первого пользователя
     * @param otherId идентификатор второго пользователя
     * @return коллекция пользователей, являющихся друзьями обоих указанных пользователей
     * @throws ru.yandex.practicum.filmorate.exception.NotFoundException если один из пользователей не найден
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> findCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.debug("Find common friends id = {} with otherId = {}", id, otherId);
        return userService.findCommonFriends(id, otherId);
    }
}