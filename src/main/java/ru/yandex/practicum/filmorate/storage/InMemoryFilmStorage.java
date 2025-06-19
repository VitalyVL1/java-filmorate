package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

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

        Film oldFilm = films.get(newFilm.getId());

        if (newFilm.getName() != null) {
            log.info("Updating film with name: {}", newFilm.getName());
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            log.info("Updating film with description: {}", newFilm.getDescription());
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            log.info("Updating film with releaseDate: {}", newFilm.getReleaseDate());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            log.info("Updating film with duration: {}", newFilm.getDuration());
            oldFilm.setDuration(newFilm.getDuration());
        }

        films.put(oldFilm.getId(), oldFilm);

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
}
