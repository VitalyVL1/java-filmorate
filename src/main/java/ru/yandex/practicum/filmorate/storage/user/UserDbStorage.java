package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.util.UserUtil;

import java.sql.Date;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_USER_QUERY = "INSERT INTO users(email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id, status) " +
            "VALUES (?, ?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends " +
            "WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_BY_USER_QUERY = "SELECT * FROM users " +
            "WHERE user_id IN (" +
            "SELECT friend_id FROM friends " +
            "WHERE user_id = ?" +
            ")";
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT * FROM users " +
            "WHERE user_id IN (" +
            "SELECT friend_id FROM friends WHERE user_id = ?" +
            "INTERSECT " +
            "SELECT friend_id FROM friends WHERE user_id = ?" +
            ")";
    private static final String FOR_MAP_FRIENDS_ID_STATUS = "SELECT friend_id, status FROM friends " +
            "WHERE user_id = ?";

    @Override
    public User create(User user) {
        long id = insert(
                INSERT_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        Optional<User> user = findOne(FIND_USER_BY_ID_QUERY, id);

        if (user.isEmpty()) {
            return Optional.empty();
        }

        user.get().getFriends().putAll(getFriends(id));

        return user;
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_USERS_QUERY)
                .stream()
                .map(user -> {
                    user.getFriends().putAll(getFriends(user.getId()));
                    return user;
                })
                .toList();
    }

    @Override
    public Optional<User> update(User newUser) {
        Optional<User> oldUserOptional = findById(newUser.getId());

        if (oldUserOptional.isPresent()) {
            User updatedUser = UserUtil.userFieldsUpdate(oldUserOptional.get(), newUser);

            update(
                    UPDATE_USER_QUERY,
                    updatedUser.getEmail(),
                    updatedUser.getLogin(),
                    updatedUser.getName(),
                    Date.valueOf(updatedUser.getBirthday()),
                    updatedUser.getId()
            );

            return Optional.of(updatedUser);
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> removeById(Long id) {
        Optional<User> deletedUserOptional = findById(id);
        delete(DELETE_USER_QUERY, id);
        return deletedUserOptional;
    }

    @Override
    public User addFriend(User user, User friend, FriendStatus friendStatus) {
        jdbc.update(
                INSERT_FRIEND_QUERY,
                user.getId(),
                friend.getId(),
                friendStatus.toString()
        );
        user.getFriends().put(friend.getId(), friendStatus);
        return user;
    }

    @Override
    public User removeFriend(User user, User friend) {
        delete(DELETE_FRIEND_QUERY, user.getId(), friend.getId());
        user.getFriends().remove(friend.getId());
        return user;
    }

    @Override
    public Collection<User> findFriends(User user) {
        return findMany(FIND_FRIENDS_BY_USER_QUERY, user.getId());
    }

    @Override
    public Collection<User> findCommonFriends(User user, User otherUser) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, user.getId(), otherUser.getId());
    }

    private Map<Long, FriendStatus> getFriends(Long userId) {
        return jdbc.queryForList(FOR_MAP_FRIENDS_ID_STATUS, userId)
                .stream()
                .collect(Collectors.toMap(
                        k -> Integer.toUnsignedLong((Integer) k.get("friend_id")), //двойной кастинг, т.к. сразу в Long в этом месте не кастится
                        v -> FriendStatus.valueOf((String) v.get("status")))
                );
    }
}
