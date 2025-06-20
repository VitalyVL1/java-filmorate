package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    Optional<User> findById(Long id);

    Collection<User> findAll();

    Optional<User> update(User newUser);

    Optional<User> removeById(Long id);

    User addFriend(User user, User friend, FriendStatus friendStatus);

    User removeFriend(User user, User friend);

    Collection<User> findFriends(User user);

    Collection<User> findCommonFriends(User user, User otherUser);
}
