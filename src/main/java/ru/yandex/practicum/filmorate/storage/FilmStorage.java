package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film create(Film film);

    Film findById(Long id);

    Collection<Film> findAll();

    Film update(Film newFilm);

    Film removeById(Long id);
}
