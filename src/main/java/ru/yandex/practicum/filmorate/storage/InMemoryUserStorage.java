package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        if (isContainsEmail(user)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin()); //копируем значение login в name если name не задан
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.remove(newUser.getId());
            if (newUser.getEmail() != null && isContainsEmail(newUser)) {
                users.put(oldUser.getId(), oldUser); //возвращаем пользователя обратно перед тем как выбросить исключение
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
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
            users.put(oldUser.getId(), oldUser);
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public User deleteById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        return users.remove(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isContainsEmail(User user) {
        return users.values().stream()
                .map(User::getEmail)
                .anyMatch(user.getEmail()::equals);
    }
}
