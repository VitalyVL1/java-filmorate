package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Optional<Film> findById(Long id);

    Collection<Film> findAll();

    Optional<Film> update(Film newFilm);

    Optional<Film> removeById(Long id);
}
