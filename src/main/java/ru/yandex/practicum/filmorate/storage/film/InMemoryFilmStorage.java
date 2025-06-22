package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genres.InMemoryGenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.InMemoryMpaStorage;
import ru.yandex.practicum.filmorate.util.FilmUtil;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    InMemoryGenreStorage inMemoryGenreStorage = new InMemoryGenreStorage();
    InMemoryMpaStorage inMemoryMpaStorage = new InMemoryMpaStorage();

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        if (film.getMpa() != null) {
            film.setMpa(getMpa(film));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.setGenres(getGenres(film));
        }

        films.put(film.getId(), film);
        return films.get(film.getId());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> update(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            return Optional.empty();
        }

        Film updatedFilm = FilmUtil.filmFieldsUpdate(films.get(newFilm.getId()), newFilm);

        if (newFilm.getMpa() != null) {
            updatedFilm.setMpa(getMpa(newFilm));
        }

        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            updatedFilm.setGenres(newFilm.getGenres());
        }

        films.put(updatedFilm.getId(), updatedFilm);

        return Optional.of(films.get(newFilm.getId()));
    }

    @Override
    public Optional<Film> removeById(Long id) {
        return Optional.ofNullable(films.remove(id));
    }

    @Override
    public Film addLike(Film film, User user) {
        film.getLikes().add(user.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film removeLike(Film film, User user) {
        film.getLikes().remove(user.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> findPopular(Integer limit) {
        Comparator<Film> comparator = Comparator.comparingInt(f -> f.getLikes().size());
        return films.values()
                .stream()
                .sorted(comparator.reversed())
                .limit(limit)
                .toList();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .reduce(Long::max)
                .orElse(0L);
        return ++currentMaxId;
    }

    private Mpa getMpa(Film film) {
        return inMemoryMpaStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Mpa не найден, указанный id = " + film.getMpa().getId()));
    }

    private Set<Genre> getGenres(Film film) {
        return film.getGenres().stream().map(
                genre -> inMemoryGenreStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Genre не найден, указанный id = " + genre.getId())))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));
    }
}
