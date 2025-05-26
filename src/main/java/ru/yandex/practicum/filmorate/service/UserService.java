package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
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

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        userStorage.update(user);
        userStorage.update(friend);

        return user;
    }

    public User removeFriend(Long id, Long friendId) {
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        userStorage.update(user);
        userStorage.update(friend);

        return user;
    }

    public Collection<User> findFriends(Long id) {
        User user = userStorage.findById(id);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .toList();
    }

    public Collection<User> findCommonFriends(Long id, Long otherId) {
        Set<Long> userFriends = userStorage.findById(id).getFriends();
        Set<Long> otherUser = userStorage.findById(otherId).getFriends();

        return userFriends.stream()
                .filter(otherUser::contains)
                .map(userStorage::findById)
                .toList();
    }
}
