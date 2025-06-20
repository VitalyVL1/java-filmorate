package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.UserUtil;

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

        User updatedUser = UserUtil.userFieldsUpdate(users.get(newUser.getId()), newUser);
        users.put(updatedUser.getId(), updatedUser);

        return Optional.of(users.get(newUser.getId()));
    }

    @Override
    public Optional<User> removeById(Long id) {
        return Optional.ofNullable(users.remove(id));
    }

    @Override
    public User addFriend(User user, User friend, FriendStatus friendStatus) {
        user.getFriends().put(friend.getId(), friendStatus);

        if (friendStatus.equals(FriendStatus.CONFIRMED)) {
            friend.getFriends().put(user.getId(), FriendStatus.CONFIRMED);
        }

        return user;
    }

    @Override
    public User removeFriend(User user, User friend) {
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
        return user;
    }

    @Override
    public Collection<User> findFriends(User user) {
        return user.getFriends()
                .keySet()
                .stream()
                .map(this.users::get)
                .toList();
    }

    @Override
    public Collection<User> findCommonFriends(User user, User otherUser) {
        Set<Long> userFriends = user.getFriends().keySet();
        Set<Long> otherUserFriends = otherUser.getFriends().keySet();

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(this.users::get)
                .toList();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .reduce(Long::max)
                .orElse(0L);
        return ++currentMaxId;
    }
}
