package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.FilmUtil;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
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

        Film updatedFilm = FilmUtil.filmFieldsUpdate(films.remove(newFilm.getId()), newFilm);
        films.put(updatedFilm.getId(), updatedFilm);

        return Optional.of(films.get(newFilm.getId()));
    }

    @Override
    public Optional<Film> removeById(Long id) {
        return Optional.ofNullable(films.remove(id));
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .reduce(Long::max)
                .orElse(0L);
        return ++currentMaxId;
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
}
