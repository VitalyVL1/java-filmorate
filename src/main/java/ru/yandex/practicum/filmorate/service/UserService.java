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

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        if (containsEmail(user)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin()); //копируем значение login в name если name не задан
        }

        return userStorage.create(user);
    }

    public User findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

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

    public User removeById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return userStorage.removeById(id)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + id + " не найден")
                );
    }

    public User addFriend(Long id, Long friendId, String status) {
        return userStorage.addFriend(
                findById(id),
                findById(friendId),
                checkStatus(status)
        );
    }

    public User removeFriend(Long id, Long friendId) {
        return userStorage.removeFriend(findById(id), findById(friendId));
    }

    public Collection<User> findFriends(Long id) {
        return userStorage.findFriends(findById(id));
    }

    public Collection<User> findCommonFriends(Long id, Long otherId) {
        return userStorage.findCommonFriends(findById(id), findById(otherId));
    }

    private boolean containsEmail(User user) {
        return userStorage.findAll().stream()
                .map(User::getEmail)
                .anyMatch(user.getEmail()::equals);
    }

    private FriendStatus checkStatus(String status) {
        try {
            return FriendStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConditionsNotMetException("Статус должен быть: " + Arrays.toString(FriendStatus.values()));
        }
    }
}
