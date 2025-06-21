package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Optional<Film> findById(Long id);

    Collection<Film> findAll();

    Optional<Film> update(Film newFilm);

    Optional<Film> removeById(Long id);

    Film addLike(Film film, User user);

    Film removeLike(Film film, User user);

    public Collection<Film> findPopular(Integer limit);
}
