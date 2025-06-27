package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genres.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.util.GenreUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
public class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;
    private static final Map<Integer, Genre> GENRES = GenreUtil.fillGenres();

    @Test
    public void testCreateGenre() {
        Genre newMpa = new Genre();
        newMpa.setName("Name");

        int id = genreStorage.create(newMpa).getId();
        Optional<Genre> foundGenre = genreStorage.findById(id);

        assertThat(foundGenre)
                .isPresent()
                .hasValueSatisfying(u -> {
                            assertThat(u).hasFieldOrPropertyWithValue("id", id);
                            assertThat(u).hasFieldOrPropertyWithValue("name", "Name");
                        }
                );

    }

    @Test
    public void testFindMpaById() {
        Genre foundGenre = genreStorage.findById(1).get();
        assertEquals(foundGenre, GENRES.get(1));
    }

    @Test
    public void testFindAllMpa() {
        Collection<Genre> foundGenres = genreStorage.findAll();
        assertTrue(foundGenres.containsAll(GENRES.values()));
    }

    @Test
    public void testUpdateMpa() {
        Genre updateGenre = genreStorage.findById(1).get();
        updateGenre.setName("NewName");
        genreStorage.update(updateGenre);

        Optional<Genre> foundMpa = genreStorage.findById(updateGenre.getId());
        assertThat(foundMpa).isPresent().get().isEqualTo(updateGenre);
    }

    @Test
    public void testRemoveMpaById() {
        Collection<Genre> allGenres = genreStorage.findAll();
        genreStorage.removeById(1);

        Collection<Genre> deleteGenre = genreStorage.findAll();
        assertEquals(allGenres.size() - 1, deleteGenre.size());

        Optional<Genre> foundGenre = genreStorage.findById(1);
        assertThat(foundGenre).isNotPresent();
    }

    @Test
    public void testContainsMpa() {
        Genre notFoundGenre = new Genre();
        notFoundGenre.setId(1000);
        notFoundGenre.setName("Name");

        assertFalse(genreStorage.contains(notFoundGenre));
        assertTrue(genreStorage.contains(GENRES.get(1)));
    }
}
