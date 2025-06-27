package ru.yandex.practicum.filmorate.storage.genres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.util.GenreUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryGenreStorage implements GenreStorage {
    private final Map<Integer, Genre> genres = GenreUtil.fillGenres();

    @Override
    public Genre create(Genre genre) {
        genre.setId(getNextId());
        genres.put(genre.getId(), genre);
        return genre;
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public Collection<Genre> findAll() {
        return genres.values();
    }

    @Override
    public Optional<Genre> update(Genre newGenre) {
        if (!genres.containsKey(newGenre.getId())) {
            return Optional.empty();
        }

        Genre oldGenre = genres.get(newGenre.getId());

        if (newGenre.getName() != null && !newGenre.getName().isBlank()) {
            oldGenre.setName(newGenre.getName());
            genres.put(oldGenre.getId(), oldGenre);
        }

        return Optional.of(oldGenre);
    }

    @Override
    public Optional<Genre> removeById(Integer id) {
        return Optional.ofNullable(genres.remove(id));
    }

    @Override
    public boolean contains(Genre genre) {
        return genres.containsKey(genre.getId());
    }

    private Integer getNextId() {
        int currentMaxId = genres.keySet()
                .stream()
                .reduce(Integer::max)
                .orElse(0);
        return ++currentMaxId;
    }
}
