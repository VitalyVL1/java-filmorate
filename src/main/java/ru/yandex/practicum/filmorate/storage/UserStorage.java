package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    public User create(User user);

    public User findById(Long id);

    public Collection<User> findAll();

    public User update(User newUser);

    public User deleteById(Long id);
}
