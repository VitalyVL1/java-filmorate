package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.util.FilmUtil;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_FILMS_QUERY = "SELECT * FROM films";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_FILM_QUERY =
            "INSERT INTO films(name, description, release_date, duration, rating_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET " +
            "name = ?, description = ?, release_date = ?, duration = ?, rating_id " +
            "WHERE film_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String LIKES_FILM_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String GENRES_FILM_QUERY = "SELECT genre_id, name FROM film_genres " +
            "JOIN genres USING(genre_id) " +
            "WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) " +
            "VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_QUERY = "SELECT * FROM films " +
            "JOIN (SELECT film_id, COUNT(user_id) AS like FROM likes GROUP BY film_id) USING(film_id)" +
            "ORDER BY like DESC LIMIT ?";
    private static final String ADD_GENRE_TO_FILM = "INSERT INTO film_genres (film_id, genre_id)" +
            "VALUES(?, ?)";
    private static final String DELETE_GENRE_BY_FILM = "DELETE FROM film_genres WHERE film_id = ?";

    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRating().getId()
        );

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            fillFilmGenres(film);
        }

        film.setId(id);
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        Optional<Film> film = findOne(FIND_FILM_BY_ID_QUERY, id);

        if (film.isEmpty()) {
            return Optional.empty();
        }

        film.get().getLikes().addAll(getFilmLikes(id));
        film.get().getGenres().addAll(getFilmGenres(id));

        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_FILMS_QUERY)
                .stream()
                .map(film -> {
                    film.getLikes().addAll(getFilmLikes(film.getId()));
                    film.getGenres().addAll(getFilmGenres(film.getId()));
                    return film;
                })
                .toList();
    }

    @Override
    public Optional<Film> update(Film newFilm) {
        Optional<Film> oldFilmOptional = findById(newFilm.getId());
        if (oldFilmOptional.isPresent()) {
            Film updatedFilm = FilmUtil.filmFieldsUpdate(oldFilmOptional.get(), newFilm);

            update(
                    UPDATE_FILM_QUERY,
                    updatedFilm.getName(),
                    updatedFilm.getDescription(),
                    Date.valueOf(updatedFilm.getReleaseDate()),
                    updatedFilm.getDuration(),
                    updatedFilm.getRating().getId(),
                    updatedFilm.getId()
            );

            updatedFilm.setLikes(getFilmLikes(updatedFilm.getId()));
            updatedFilm.setGenres(getFilmGenres(updatedFilm.getId()));

            if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
                deleteFilmGenres(updatedFilm);
                updatedFilm.setGenres(getFilmGenres(updatedFilm.getId()));
            }

            return Optional.of(updatedFilm);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Film> removeById(Long id) {
        Optional<Film> deletedFilmOptional = findById(id);
        delete(DELETE_FILM_QUERY, id);
        return deletedFilmOptional;
    }

    @Override
    public Film addLike(Film film, User user) {
        jdbc.update(
                ADD_LIKE_QUERY,
                film.getId(),
                user.getId()
        );
        film.getLikes().add(user.getId());
        return film;
    }

    @Override
    public Film removeLike(Film film, User user) {
        delete(
                DELETE_LIKE_QUERY,
                film.getId(),
                user.getId()
        );
        film.getLikes().remove(user.getId());
        return film;
    }

    @Override
    public Collection<Film> findPopular(Integer limit) {
        return findMany(FIND_POPULAR_QUERY, limit).stream()
                .map(film -> {
                    film.setLikes(getFilmLikes(film.getId()));
                    film.setGenres(getFilmGenres(film.getId()));
                    return film;
                })
                .toList();
    }

    private Set<Long> getFilmLikes(Long id) {
        return jdbc.queryForList(LIKES_FILM_QUERY, id)
                .stream()
                .map(x -> Integer.toUnsignedLong((Integer) x.get("user_id")))
                .collect(Collectors.toSet());
    }

    private Set<Genre> getFilmGenres(Long id) {
        return jdbc.queryForList(GENRES_FILM_QUERY, id)
                .stream()
                .map(x -> {
                    Genre genre = new Genre();
                    genre.setId((Integer) x.get("genre_id"));
                    genre.setName(x.get("name").toString());
                    return genre;
                })
                .collect(Collectors.toSet());
    }

    private Set<Genre> fillFilmGenres(Film film) {
        for (Genre genre : film.getGenres()) {
            jdbc.update(ADD_GENRE_TO_FILM, film.getId(), genre.getId());
        }
        return film.getGenres();
    }

    private void deleteFilmGenres(Film film) {
        jdbc.update(DELETE_GENRE_BY_FILM, film.getId());
    }
}
