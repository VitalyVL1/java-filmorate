package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WebMvcTest(User.class)
public class UserTest {
    private static final String CORRECT_EMAIL = "ivan@gmail.com";
    private static final String CORRECT_LOGIN = "Vanya";
    private static final String CORRECT_NAME = "Ivan";
    private static final LocalDate CORRECT_BIRTHDAY = LocalDate.of(1900, 1, 1);

    private static final String INCORRECT_EMAIL = "1234abc";
    private static final String INCORRECT_LOGIN = "Van ya";
    private static final LocalDate INCORRECT_BIRTHDAY = LocalDate.of(3000, 1, 1);

    @Autowired
    Validator validator;

    @Test
    void correctFilmTest() {
        User user = new User();
        user.setEmail(CORRECT_EMAIL);
        user.setName(CORRECT_NAME);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThrows(RuntimeException.class, () -> violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации")));
    }

    @Test
    void blankEmailTest() {
        User user = new User();
        user.setEmail("");
        user.setName(CORRECT_NAME);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        ConstraintViolation<User> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Email не может быть пустым", violation.getMessageTemplate());
    }

    @Test
    void blankLoginTest() {
        User user = new User();
        user.setEmail(CORRECT_EMAIL);
        user.setName(CORRECT_NAME);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        ConstraintViolation<User> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("логин не может быть пустым", violation.getMessageTemplate());
    }

    @Test
    void incorrectEmailTest() {
        User user = new User();
        user.setEmail(INCORRECT_EMAIL);
        user.setName(CORRECT_NAME);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        ConstraintViolation<User> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("email", violation.getPropertyPath().toString());
        assertEquals("Введен некорректный email", violation.getMessageTemplate());
    }

    @Test
    void incorrectLoginTest() {
        User user = new User();
        user.setEmail(CORRECT_EMAIL);
        user.setName(CORRECT_NAME);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(INCORRECT_LOGIN);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        ConstraintViolation<User> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("login", violation.getPropertyPath().toString());
        assertEquals("логин не может содержать пробелы", violation.getMessageTemplate());
    }

    @Test
    void incorrectBirthdayTest() {
        User user = new User();
        user.setEmail(CORRECT_EMAIL);
        user.setName(CORRECT_NAME);
        user.setBirthday(INCORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        ConstraintViolation<User> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("birthday", violation.getPropertyPath().toString());
        assertEquals("Дата рождения не может быть в будущем", violation.getMessageTemplate());
    }
}
