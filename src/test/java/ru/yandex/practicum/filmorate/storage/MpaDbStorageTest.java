package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.util.MpaUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
public class MpaDbStorageTest {
    private final MpaDbStorage mpaStorage;
    private static final Map<Integer, Mpa> MPA = MpaUtil.fillMpa();

    @Test
    public void testCreateMpa() {
        Mpa newMpa = new Mpa();
        newMpa.setName("Name");
        newMpa.setDescription("Description");

        int id = mpaStorage.create(newMpa).getId();
        Optional<Mpa> foundMpa = mpaStorage.findById(id);

        assertThat(foundMpa)
                .isPresent()
                .hasValueSatisfying(u -> {
                            assertThat(u).hasFieldOrPropertyWithValue("id", id);
                            assertThat(u).hasFieldOrPropertyWithValue("name", "Name");
                            assertThat(u).hasFieldOrPropertyWithValue("description", "Description");
                        }
                );

    }

    @Test
    public void testFindMpaById() {
        Mpa foundMpa = mpaStorage.findById(1).get();
        assertEquals(foundMpa, MPA.get(1));
    }

    @Test
    public void testFindAllMpa() {
        Collection<Mpa> foundMpa = mpaStorage.findAll();
        assertTrue(foundMpa.containsAll(MPA.values()));
    }

    @Test
    public void testUpdateMpa() {
        Mpa updateMpa = mpaStorage.findById(1).get();
        updateMpa.setName("NewName");
        updateMpa.setDescription("UpdatedDescription");
        mpaStorage.update(updateMpa);

        Optional<Mpa> foundMpa = mpaStorage.findById(updateMpa.getId());
        assertThat(foundMpa).isPresent().get().isEqualTo(updateMpa);
    }

    @Test
    public void testRemoveMpaById() {
        Collection<Mpa> allMpa = mpaStorage.findAll();
        mpaStorage.removeById(1);

        Collection<Mpa> deleteMpa = mpaStorage.findAll();
        assertEquals(allMpa.size() - 1, deleteMpa.size());

        Optional<Mpa> foundMpa = mpaStorage.findById(1);
        assertThat(foundMpa).isNotPresent();
    }

    @Test
    public void testContainsMpa() {
        Mpa notFoundMpa = new Mpa();
        notFoundMpa.setId(1000);
        notFoundMpa.setName("Name");
        notFoundMpa.setDescription("Description");

        assertFalse(mpaStorage.contains(notFoundMpa));
        assertTrue(mpaStorage.contains(MPA.get(1)));
    }
}
