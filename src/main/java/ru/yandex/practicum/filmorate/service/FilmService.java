package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(
            @Qualifier("filmStorageAlias") FilmStorage filmStorage,
            @Qualifier("userStorageAlias") UserStorage userStorage,
            @Qualifier("mpaStorageAlias") MpaStorage mpaStorage,
            @Qualifier("genreStorageAlias") GenreStorage genreStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film create(Film film) {
        checkMpaAndGenre(film);
        return filmStorage.create(film);
    }

    public Film findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        checkMpaAndGenre(newFilm);

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
        return filmStorage.addLike(findById(id), getUser(userId));
    }

    public Film removeLike(Long id, Long userId) {
        return filmStorage.removeLike(findById(id), getUser(userId));
    }

    public Collection<Film> findPopular(Integer limit) {
        return filmStorage.findPopular(limit);
    }

    private User getUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + id + " не найден")
                );
    }

    private void checkMpaAndGenre(Film film) {
        if (film.getMpa() != null && !mpaStorage.contains(film.getMpa())) {
            throw new NotFoundException("Mpa не найден, указанный id = " + film.getMpa().getId());
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.contains(genre)) {
                    throw new NotFoundException("Genre не найден, указанный id = " + genre.getId());
                }
            }
        }
    }
}
