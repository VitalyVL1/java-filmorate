package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.FriendStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserDbStorageTest {
    private final UserDbStorage userStorage;

    private User user;
    private FriendStatus friendStatus = FriendStatus.UNCONFIRMED;

    private static final String CORRECT_LOGIN = "Login";
    private static final String CORRECT_NAME = "Name";
    private static final String CORRECT_EMAIL = "email@email.com";
    private static final LocalDate CORRECT_BIRTHDAY = LocalDate.of(1986, 10, 2);

    private static final String UPDATE_LOGIN = "update_login";
    private static final String UPDATE_NAME = "update_name";
    private static final String UPDATE_EMAIL = "update_email@email.com";
    private static final LocalDate UPDATE_BIRTHDAY = LocalDate.of(1989, 6, 24);

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin(CORRECT_LOGIN);
        user.setName(CORRECT_NAME);
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
    }

    @Test
    public void testCreateUser() {
        User createdUser = userStorage.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(user.getLogin(), createdUser.getLogin());
        assertEquals(user.getName(), createdUser.getName());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getBirthday(), createdUser.getBirthday());

    }

    @Test
    public void testFindUserById() {
        Long id = userStorage.create(user).getId();
        Optional<User> userOptional = userStorage.findById(id);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                            assertThat(u).hasFieldOrPropertyWithValue("id", id);
                            assertThat(u).hasFieldOrPropertyWithValue("name", CORRECT_NAME);
                            assertThat(u).hasFieldOrPropertyWithValue("email", CORRECT_EMAIL);
                            assertThat(u).hasFieldOrPropertyWithValue("login", CORRECT_LOGIN);
                            assertThat(u).hasFieldOrPropertyWithValue("birthday", CORRECT_BIRTHDAY);
                        }
                );
    }

    @Test
    public void testUpdateUser() {
        User updatedUser = userStorage.create(user);

        updatedUser.setName(UPDATE_NAME);
        updatedUser.setEmail(UPDATE_EMAIL);
        updatedUser.setLogin(UPDATE_LOGIN);
        updatedUser.setBirthday(UPDATE_BIRTHDAY);

        userStorage.update(updatedUser);

        Optional<User> userOptional = userStorage.findById(updatedUser.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                            assertThat(u).hasFieldOrPropertyWithValue("id", updatedUser.getId());
                            assertThat(u).hasFieldOrPropertyWithValue("name", UPDATE_NAME);
                            assertThat(u).hasFieldOrPropertyWithValue("email", UPDATE_EMAIL);
                            assertThat(u).hasFieldOrPropertyWithValue("login", UPDATE_LOGIN);
                            assertThat(u).hasFieldOrPropertyWithValue("birthday", UPDATE_BIRTHDAY);
                        }
                );
    }

    @Test
    public void testFindAll() {
        User createdUser = new User();
        createdUser.setLogin(UPDATE_LOGIN);
        createdUser.setName(UPDATE_NAME);
        createdUser.setEmail(UPDATE_EMAIL);
        createdUser.setBirthday(UPDATE_BIRTHDAY);

        userStorage.create(createdUser);
        userStorage.create(user);

        List<User> users = userStorage.findAll().stream().toList();
        assertThat(users)
                .hasSize(2)
                .contains(user)
                .contains(createdUser);
    }

    @Test
    public void testRemoveUserById() {
        Long id = userStorage.create(user).getId();

        Optional<User> userOptional = userStorage.findById(id);
        assertThat(userOptional).isPresent().get().isEqualTo(user);

        userStorage.removeById(id);

        Optional<User> userDeleteOptional = userStorage.findById(id);
        assertThat(userDeleteOptional).isEmpty();
    }

    @Test
    public void testAddAndGetRemoveFriend() {
        User createdUser = new User();
        createdUser.setLogin(UPDATE_LOGIN);
        createdUser.setName(UPDATE_NAME);
        createdUser.setEmail(UPDATE_EMAIL);
        createdUser.setBirthday(UPDATE_BIRTHDAY);

        userStorage.create(createdUser);
        userStorage.create(user);

        userStorage.addFriend(user, createdUser, friendStatus);
        Optional<User> friendUser = userStorage.findFriends(user).stream().findFirst();
        assertThat(friendUser).isPresent().get().isEqualTo(createdUser);

        Optional<User> friendCreatedUser = userStorage.findFriends(createdUser).stream().findFirst();
        assertThat(friendCreatedUser).isEmpty();

        userStorage.removeFriend(user, createdUser);
        Optional<User> friendRemovedUser = userStorage.findFriends(user).stream().findFirst();
        assertThat(friendRemovedUser).isEmpty();
    }

    @Test
    public void testAddFriendUnconfirmed(){
        User createdUser = new User();
        createdUser.setLogin(UPDATE_LOGIN);
        createdUser.setName(UPDATE_NAME);
        createdUser.setEmail(UPDATE_EMAIL);
        createdUser.setBirthday(UPDATE_BIRTHDAY);

        userStorage.create(createdUser);
        userStorage.create(user);

        userStorage.addFriend(user, createdUser, FriendStatus.UNCONFIRMED);
        Optional<User> friendUser = userStorage.findFriends(user).stream().findFirst();
        assertThat(friendUser).isPresent().get().isEqualTo(createdUser);

        Optional<User> friendCreatedUser = userStorage.findFriends(createdUser).stream().findFirst();
        assertThat(friendCreatedUser).isEmpty();
    }

    @Test
    public void testFindCommonFriends() {
        User user1 = new User();
        user1.setLogin(UPDATE_LOGIN);
        user1.setName(UPDATE_NAME);
        user1.setEmail(UPDATE_EMAIL);
        user1.setBirthday(UPDATE_BIRTHDAY);

        User user2 = new User();
        user2.setLogin("Login2");
        user2.setName(UPDATE_NAME);
        user2.setEmail("email2@email.com");
        user2.setBirthday(UPDATE_BIRTHDAY);

        userStorage.create(user);
        userStorage.create(user1);
        userStorage.create(user2);

        userStorage.addFriend(user, user1, friendStatus);
        userStorage.addFriend(user2, user1, friendStatus);

        Optional<User> commonFriend = userStorage.findCommonFriends(user, user2).stream().findFirst();
        assertThat(commonFriend).isPresent().get().isEqualTo(user1);
    }
}
