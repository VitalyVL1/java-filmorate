package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> update(User newUser) {
        if (!users.containsKey(newUser.getId())) {
            return Optional.empty();
        }

        User oldUser = users.get(newUser.getId());

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

        return Optional.of(users.get(newUser.getId()));
    }

    @Override
    public Optional<User> removeById(Long id) {
        return Optional.ofNullable(users.remove(id));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .reduce(Long::max)
                .orElse(0L);
        return ++currentMaxId;
    }
}
