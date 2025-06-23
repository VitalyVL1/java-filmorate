package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.util.GenreUtil;
import ru.yandex.practicum.filmorate.util.MpaUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film film;

    private static final Map<Integer, Mpa> MPA = MpaUtil.fillMpa();
    private static final Map<Integer, Genre> GENRES = GenreUtil.fillGenres();

    private static final String CORRECT_NAME = "Name";
    private static final String CORRECT_DESCRIPTION = "Description";
    private static final LocalDate CORRECT_RELEASE_DATE = LocalDate.of(1995, 1, 1);
    private static final int CORRECT_DURATION = 100;
    private static final Mpa CORRECT_MPA = MPA.get(1);
    private static final Genre CORRECT_GENRE = GENRES.get(1);

    private static final String UPDATE_NAME = "UPDATE_Name";
    private static final String UPDATE_DESCRIPTION = "UPDATE_Description";
    private static final LocalDate UPDATE_RELEASE_DATE = LocalDate.of(2000, 12, 31);
    private static final int UPDATE_DURATION = 200;
    private static final Mpa UPDATE_MPA = MPA.get(2);
    private static final Genre UPDATE_GENRE = GENRES.get(2);

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);
        film.setMpa(CORRECT_MPA);
        film.getGenres().add(CORRECT_GENRE);
    }

    @Test
    public void testCreateFilm() {
        Film createdFilm = filmStorage.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals(film.getName(), createdFilm.getName());
        assertEquals(film.getDescription(), createdFilm.getDescription());
        assertEquals(film.getDuration(), createdFilm.getDuration());
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate());
        assertEquals(film.getMpa(), createdFilm.getMpa());
        assertTrue(film.getGenres().containsAll(createdFilm.getGenres()));
    }

    @Test
    public void testFindById() {
        Long id = filmStorage.create(film).getId();

        Optional<Film> filmOptional = filmStorage.findById(id);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                            assertThat(u).hasFieldOrPropertyWithValue("id", id);
                            assertThat(u).hasFieldOrPropertyWithValue("name", CORRECT_NAME);
                            assertThat(u).hasFieldOrPropertyWithValue("mpa", CORRECT_MPA);
                            assertThat(u).hasFieldOrPropertyWithValue("description", CORRECT_DESCRIPTION);
                            assertThat(u).hasFieldOrPropertyWithValue("releaseDate", CORRECT_RELEASE_DATE);
                            assertThat(u).hasFieldOrPropertyWithValue("duration", CORRECT_DURATION);
                        }
                );

        assertTrue(filmOptional.get().getGenres().containsAll(film.getGenres()));
    }

    @Test
    public void testUpdateFilm() {
        Film updateFilm = filmStorage.create(film);
        updateFilm.setName(UPDATE_NAME);
        updateFilm.setDescription(UPDATE_DESCRIPTION);
        updateFilm.setDuration(UPDATE_DURATION);
        updateFilm.setReleaseDate(UPDATE_RELEASE_DATE);
        updateFilm.setMpa(UPDATE_MPA);
        updateFilm.getGenres().add(UPDATE_GENRE);

        filmStorage.update(updateFilm);

        Optional<Film> filmOptional = filmStorage.findById(updateFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                            assertThat(u).hasFieldOrPropertyWithValue("id", updateFilm.getId());
                            assertThat(u).hasFieldOrPropertyWithValue("name", UPDATE_NAME);
                            assertThat(u).hasFieldOrPropertyWithValue("mpa", UPDATE_MPA);
                            assertThat(u).hasFieldOrPropertyWithValue("description", UPDATE_DESCRIPTION);
                            assertThat(u).hasFieldOrPropertyWithValue("releaseDate", UPDATE_RELEASE_DATE);
                            assertThat(u).hasFieldOrPropertyWithValue("duration", UPDATE_DURATION);
                        }
                );

        assertTrue(filmOptional.get().getGenres().containsAll(updateFilm.getGenres()));
    }

    @Test
    public void testFindAllFilm() {
        Film createdFilm = new Film();
        createdFilm.setName(UPDATE_NAME);
        createdFilm.setDescription(UPDATE_DESCRIPTION);
        createdFilm.setDuration(UPDATE_DURATION);
        createdFilm.setReleaseDate(UPDATE_RELEASE_DATE);
        createdFilm.setMpa(UPDATE_MPA);
        createdFilm.getGenres().add(UPDATE_GENRE);

        filmStorage.create(film);
        filmStorage.create(createdFilm);

        List<Film> users = filmStorage.findAll().stream().toList();
        assertThat(users)
                .hasSize(2)
                .contains(film)
                .contains(createdFilm);
    }

    @Test
    public void testRemoveFilmById() {
        Long id = filmStorage.create(film).getId();

        Optional<Film> filmOptional = filmStorage.findById(id);
        assertThat(filmOptional).isPresent().get().isEqualTo(film);

        filmStorage.removeById(id);

        Optional<Film> filmDeleteOptional = filmStorage.findById(id);
        assertThat(filmDeleteOptional).isNotPresent();
    }

    @Test
    public void testAddAndGetAndRemoveLike() {
        Film createdFilm = filmStorage.create(film);
        User user = createUser();

        filmStorage.addLike(createdFilm, user);

        Optional<Film> filmAddLikeOptional = filmStorage.findById(createdFilm.getId());
        assertThat(filmAddLikeOptional).isPresent();
        assertTrue(filmAddLikeOptional.get().getLikes().contains(user.getId()));

        filmStorage.removeLike(createdFilm, user);
        Optional<Film> removeLikeOptional = filmStorage.findById(createdFilm.getId());
        assertThat(removeLikeOptional).isPresent();
        assertTrue(removeLikeOptional.get().getLikes().isEmpty());
    }

    @Test
    public void testFindPopularFilm() {
        Film filmNoLikes = new Film();
        filmNoLikes.setName(UPDATE_NAME);
        filmNoLikes.setDescription(UPDATE_DESCRIPTION);
        filmNoLikes.setDuration(UPDATE_DURATION);
        filmNoLikes.setReleaseDate(UPDATE_RELEASE_DATE);
        filmNoLikes.setMpa(UPDATE_MPA);
        filmNoLikes.getGenres().add(UPDATE_GENRE);
        filmStorage.create(filmNoLikes);

        Film filmLike = filmStorage.create(film);
        User user = createUser();

        filmStorage.addLike(filmLike, user);

        Optional<Film> topFilm = filmStorage.findPopular(1).stream().findFirst();
        assertThat(topFilm).isPresent().get().isEqualTo(filmLike);

    }

    private User createUser() {
        User user = new User();
        user.setLogin("Login");
        user.setName("Name");
        user.setEmail("email@email.com");
        user.setBirthday(LocalDate.of(1986, 10, 2));
        return userStorage.create(user);
    }
}
