package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {
    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY mpa_id";
    private static final String FIND_BY_ID = "SELECT * FROM mpa WHERE mpa_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO mpa(name, description) VALUES (?, ?)";
    private static final String UPDATE_QUERY = "UPDATE mpa SET name = ?, description = ? WHERE mpa_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM mpa WHERE mpa_id = ?";

    @Override
    public Mpa create(Mpa mpa) {
        int id = (int) insert(INSERT_QUERY, mpa.getName());
        mpa.setId(id);
        return mpa;
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public Collection<Mpa> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Mpa> update(Mpa newMpa) {
        Optional<Mpa> oldMpa = findById(newMpa.getId());

        if (oldMpa.isPresent()) {
            Mpa updatedMpa = oldMpa.get();
            if (!newMpa.getName().isBlank()) {
                updatedMpa.setName(newMpa.getName());
            }
            if (!newMpa.getDescription().isBlank()) {
                updatedMpa.setDescription(newMpa.getDescription());
            }
            update(UPDATE_QUERY, updatedMpa.getName(), updatedMpa.getDescription(), updatedMpa.getId());
            return Optional.of(updatedMpa);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Mpa> removeById(Integer id) {
        Optional<Mpa> deletedMpaOptional = findById(id);
        delete(DELETE_QUERY, id);
        return deletedMpaOptional;
    }

    @Override
    public boolean contains(Mpa mpa) {
        return findAll().stream()
                .anyMatch(mpa::equals);
    }
}
