package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User user);

    User findById(Long id);

    Collection<User> findAll();

    User update(User newUser);

    User removeById(Long id);
}
