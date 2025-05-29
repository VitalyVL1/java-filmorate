package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(
            FilmStorage filmStorage,
            UserStorage userStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        film.setId(getNextId());
        return filmStorage.create(film);
    }

    public Film findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return getFilm(id);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return filmStorage.update(newFilm)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден"));
    }

    public Film removeById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return filmStorage.removeById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }


    public Film addLike(Long id, Long userId) {
        Film film = getFilm(id);
        User user = getUser(userId);
        film.getLikes().add(user.getId());
        return filmStorage.update(film).get(); //дополнительные действия не требуются т.к. уже наверняка известно, что фильм в списке
    }

    public Film removeLike(Long id, Long userId) {
        Film film = getFilm(id);
        User user = getUser(userId);
        film.getLikes().remove(user.getId());
        return filmStorage.update(film).get(); //дополнительные действия не требуются т.к. уже наверняка известно, что фильм в списке
    }

    public Collection<Film> findPopular(Integer count) {
        Comparator<Film> comparator = Comparator.comparingInt(f -> f.getLikes().size());
        return filmStorage.findAll().stream()
                .sorted(comparator.reversed())
                .limit(count)
                .toList();
    }

    private long getNextId() {
        long currentMaxId = filmStorage.findAll()
                .stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private Film getFilm(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    private User getUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + id + " не найден")
                );
    }
}
