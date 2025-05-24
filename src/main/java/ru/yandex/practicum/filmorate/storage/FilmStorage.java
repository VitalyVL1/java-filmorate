package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    public Film create(Film film);

    public Film findById(Long id);

    public Collection<Film> findAll();

    public Film update(Film newFilm);

    public Film deleteById(Long id);
}
