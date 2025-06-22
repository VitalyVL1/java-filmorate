package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;

import java.util.Collection;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("genreStorageAlias") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genre create(Genre genre) {
        if (containsName(genre)) {
            throw new DuplicatedDataException("Такой жанр уже существует");
        }
        return genreStorage.create(genre);
    }

    public Genre findById(Integer id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }

    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre update(Genre newGenre) {
        Genre oldGenre = findById(newGenre.getId());

        if (newGenre.getName() != null &&
                !newGenre.getName().equals(oldGenre.getName()) &&
                containsName(newGenre)) {
            throw new DuplicatedDataException("Такой жанр уже существует");
        }

        return genreStorage.update(newGenre)
                .orElseThrow(
                        () -> new NotFoundException("Жанр с id = " + newGenre.getId() + " не найден")
                );
    }

    private boolean containsName(Genre genre) {
        return genreStorage.findAll().stream()
                .map(Genre::getName)
                .anyMatch(genre.getName()::equals);
    }

    public boolean containsGenre(Genre genre) {
        return genreStorage.findAll().stream()
                .anyMatch(genre::equals);
    }
}
