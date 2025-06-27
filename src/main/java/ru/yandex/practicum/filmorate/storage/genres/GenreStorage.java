package ru.yandex.practicum.filmorate.storage.genres;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    Genre create(Genre genre);

    Optional<Genre> findById(Integer id);

    Collection<Genre> findAll();

    Optional<Genre> update(Genre newGenre);

    Optional<Genre> removeById(Integer id);

    boolean contains(Genre genre);
}
