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

@WebMvcTest(Film.class)
public class FilmTest {

    private static final String CORRECT_NAME = "Toy Story";
    private static final String CORRECT_DESCRIPTION = "Good movie";
    private static final LocalDate CORRECT_RELEASE_DATE = LocalDate.of(1995, 1, 1);
    private static final Integer CORRECT_DURATION = 90;

    private static final String INCORRECT_NAME = "";
    private static final String INCORRECT_DESCRIPTION = "G".repeat(201);
    private static final LocalDate INCORRECT_RELEASE_DATE = LocalDate.of(1895, 1, 1);
    private static final Integer INCORRECT_DURATION = -90;

    @Autowired
    Validator validator;

    @Test
    void correctFilmTest() {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThrows(RuntimeException.class, () -> violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации")));
    }

    @Test
    void blankNameTest() {
        Film film = new Film();
        film.setName(INCORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        ConstraintViolation<Film> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Название не может быть пустым", violation.getMessageTemplate());
    }

    @Test
    void exceededLengthDescriptionTest() {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(INCORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        ConstraintViolation<Film> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("description", violation.getPropertyPath().toString());
        assertEquals("Максимальная длина описания — 200 символов", violation.getMessageTemplate());
    }

    @Test
    void IncorrectReleaseDateTest() {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(INCORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        ConstraintViolation<Film> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("releaseDate", violation.getPropertyPath().toString());
        assertEquals("Дата должна быть не раньше 28 декабря 1895", violation.getMessageTemplate());
    }

    @Test
    void negativeDurationTest() {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(INCORRECT_DURATION);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        ConstraintViolation<Film> violation = violations.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Отсутствует ошибка валидации"));

        assertEquals("duration", violation.getPropertyPath().toString());
        assertEquals("Продолжительность фильма должна быть положительным числом", violation.getMessageTemplate());
    }
}
