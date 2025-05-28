package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        if (userStorage.containsEmail(user)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin()); //копируем значение login в name если name не задан
        }

        user.setId(getNextId());

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
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User oldUser = getUser(newUser.getId());

        if (newUser.getEmail() != null &&
                !newUser.getEmail().equals(oldUser.getEmail()) &&
                userStorage.containsEmail(newUser)) {
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

    public User addFriend(Long id, Long friendId) {
        User user = getUser(id);
        User friend = getUser(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        userStorage.update(user);
        userStorage.update(friend);

        return user;
    }

    public User removeFriend(Long id, Long friendId) {
        User user = getUser(id);
        User friend = getUser(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        userStorage.update(user);
        userStorage.update(friend);

        return user;
    }

    public Collection<User> findFriends(Long id) {
        return getUser(id)
                .getFriends()
                .stream()
                .map(this::getUser)
                .toList();
    }

    public Collection<User> findCommonFriends(Long id, Long otherId) {
        Set<Long> userFriends = getUser(id).getFriends();
        Set<Long> otherUser = getUser(otherId).getFriends();

        return userFriends.stream()
                .filter(otherUser::contains)
                .map(this::getUser)
                .toList();
    }

    private long getNextId() {
        long currentMaxId = userStorage.findAll()
                .stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private User getUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + id + " не найден")
                );
    }
}
