package ru.yandex.practicum.filmorate.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class UserUtil {
    public static User userFieldsUpdate(User oldUser, User newUser) {
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
        return oldUser;
    }
}
