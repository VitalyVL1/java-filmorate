package ru.yandex.practicum.filmorate.storage.genres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {
    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO genres(name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE genres SET name = ? WHERE genre_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM genres WHERE genre_id = ?";


    @Override
    public Genre create(Genre genre) {
        int id = (int) insert(INSERT_QUERY, genre.getName());
        genre.setId(id);
        return genre;
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> update(Genre newGenre) {
        Optional<Genre> oldGenre = findById(newGenre.getId());

        if (oldGenre.isPresent()) {
            Genre updatedGenre = oldGenre.get();
            if (!newGenre.getName().isBlank()) {
                updatedGenre.setName(newGenre.getName());
            }
            update(UPDATE_QUERY, updatedGenre.getName(), updatedGenre.getId());
            return Optional.of(updatedGenre);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Genre> removeById(Integer id) {
        Optional<Genre> deletedGenreOptional = findById(id);
        delete(DELETE_QUERY, id);
        return deletedGenreOptional;
    }

    @Override
    public boolean contains(Genre genre) {
        return findAll().stream()
                .anyMatch(genre::equals);
    }
}
