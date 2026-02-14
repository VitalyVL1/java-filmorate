package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.util.FilmUtil;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация хранилища фильмов на основе базы данных (H2).
 * <p>
 * Обеспечивает хранение (persistence) данных о фильмах, жанрах, рейтингах MPA
 * и лайках в реляционной базе данных. Использует {@link JdbcTemplate} для
 * выполнения SQL-запросов и {@link RowMapper} для преобразования результатов
 * запросов в объекты {@link Film}.
 * </p>
 *
 * <p>Основные особенности:
 * <ul>
 *   <li>Автоматическое управление связью многие-ко-многим между фильмами и жанрами</li>
 *   <li>Подсчет и хранение лайков пользователей</li>
 *   <li>Полная загрузка связанных сущностей (MPA, жанры, лайки) при каждом запросе</li>
 *   <li>Транзакционность операций обновления жанров</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.storage.film.FilmStorage
 * @see ru.yandex.practicum.filmorate.storage.BaseRepository
 */
@Slf4j
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    /** SQL-запрос для получения всех фильмов */
    private static final String FIND_ALL_FILMS_QUERY = "SELECT * FROM films";

    /** SQL-запрос для получения фильма по ID */
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";

    /** SQL-запрос для вставки нового фильма */
    private static final String INSERT_FILM_QUERY =
            "INSERT INTO films(name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";

    /** SQL-запрос для обновления существующего фильма */
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET " +
                                                    "name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                                                    "WHERE film_id = ?";

    /** SQL-запрос для удаления фильма */
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    /** SQL-запрос для получения лайков фильма */
    private static final String LIKES_FILM_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";

    /** SQL-запрос для получения жанров фильма с сортировкой по ID */
    private static final String GENRES_FILM_QUERY = "SELECT fg.genre_id, name FROM film_genres fg " +
                                                    "JOIN genres USING(genre_id) " +
                                                    "WHERE film_id = ?" +
                                                    "ORDER BY fg.genre_id";

    /** SQL-запрос для добавления лайка */
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) " +
                                                 "VALUES (?, ?)";

    /** SQL-запрос для удаления лайка */
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    /** SQL-запрос для получения популярных фильмов с сортировкой по количеству лайков */
    private static final String FIND_POPULAR_QUERY = "SELECT * FROM films " +
                                                     "JOIN (SELECT film_id, COUNT(user_id) AS lks FROM likes GROUP BY film_id) AS l USING(film_id)" +
                                                     "ORDER BY lks DESC LIMIT ?";

    /** SQL-запрос для добавления связи фильма с жанром */
    private static final String ADD_GENRE_TO_FILM = "INSERT INTO film_genres (film_id, genre_id)" +
                                                    "VALUES(?, ?)";

    /** SQL-запрос для удаления всех жанров фильма */
    private static final String DELETE_GENRE_BY_FILM = "DELETE FROM film_genres WHERE film_id = ?";

    /** SQL-запрос для получения информации о рейтинге MPA */
    private static final String FIND_MPA_BY_ID = "SELECT * FROM mpa WHERE mpa_id = ?";

    /**
     * Конструктор хранилища фильмов.
     *
     * @param jdbc шаблон JDBC для работы с базой данных
     * @param mapper маппер для преобразования результата запроса в объект {@link Film}
     */
    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Создает новый фильм в базе данных.
     * <p>
     * После сохранения основной информации о фильме, также сохраняет:
     * <ul>
     *   <li>Связи с жанрами (если указаны)</li>
     *   <li>Информацию о рейтинге MPA</li>
     * </ul>
     * </p>
     *
     * @param film объект фильма для сохранения
     * @return сохраненный фильм с заполненными ID, жанрами и MPA
     */
    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );

        film.setMpa(getMpa(film));
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            fillFilmGenres(film);
            film.setGenres(getFilmGenres(film.getId()));
        }

        return film;
    }

    /**
     * Находит фильм по его идентификатору.
     * <p>
     * При загрузке фильма также загружает связанные данные:
     * лайки, жанры и полную информацию о рейтинге MPA.
     * </p>
     *
     * @param id идентификатор фильма
     * @return {@link Optional} с полностью загруженным фильмом, или пустой {@link Optional},
     *         если фильм не найден
     */
    @Override
    public Optional<Film> findById(Long id) {
        Optional<Film> filmOptional = findOne(FIND_FILM_BY_ID_QUERY, id);

        if (filmOptional.isEmpty()) {
            return Optional.empty();
        }

        Film film = filmOptional.get();

        film.setLikes(getFilmLikes(id));
        film.setGenres(getFilmGenres(id));
        film.setMpa(getMpa(film));

        return Optional.of(film);
    }

    /**
     * Возвращает список всех фильмов.
     * <p>
     * Для каждого фильма загружает связанные данные (лайки, жанры, MPA).
     * </p>
     *
     * @return коллекция всех фильмов с полной информацией
     */
    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_FILMS_QUERY)
                .stream()
                .map(film -> {
                    film.setLikes(getFilmLikes(film.getId()));
                    film.setGenres(getFilmGenres(film.getId()));
                    film.setMpa(getMpa(film));
                    return film;
                })
                .toList();
    }

    /**
     * Обновляет существующий фильм.
     * <p>
     * Обновляет основную информацию о фильме. Если в обновляемом фильме
     * указаны новые жанры, старые связи удаляются и добавляются новые.
     * </p>
     *
     * @param newFilm объект фильма с обновленными данными
     * @return {@link Optional} с обновленным фильмом, или пустой {@link Optional},
     *         если фильм не найден
     */
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
                    updatedFilm.getMpa().getId(),
                    updatedFilm.getId()
            );

            updatedFilm.setLikes(getFilmLikes(updatedFilm.getId()));

            if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
                deleteFilmGenres(updatedFilm);
                fillFilmGenres(updatedFilm);
                updatedFilm.setGenres(getFilmGenres(updatedFilm.getId()));
            }

            return Optional.of(updatedFilm);
        }

        return Optional.empty();
    }

    /**
     * Удаляет фильм по идентификатору.
     * <p>
     * При удалении фильма каскадно удаляются связанные записи:
     * лайки и связи с жанрами (благодаря ON DELETE CASCADE в схеме БД).
     * </p>
     *
     * @param id идентификатор фильма
     * @return {@link Optional} с удаленным фильмом, или пустой {@link Optional},
     *         если фильм не найден
     */
    @Override
    public Optional<Film> removeById(Long id) {
        Optional<Film> deletedFilmOptional = findById(id);
        delete(DELETE_FILM_QUERY, id);
        return deletedFilmOptional;
    }

    /**
     * Добавляет лайк фильму от пользователя.
     *
     * @param film фильм, которому ставится лайк
     * @param user пользователь, ставящий лайк
     * @return обновленный фильм (множество лайков обновлено в памяти)
     */
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

    /**
     * Удаляет лайк пользователя с фильма.
     *
     * @param film фильм, с которого удаляется лайк
     * @param user пользователь, удаляющий лайк
     * @return обновленный фильм (множество лайков обновлено в памяти)
     */
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

    /**
     * Возвращает список наиболее популярных фильмов.
     * <p>
     * Фильмы сортируются по убыванию количества лайков.
     * Для каждого фильма загружаются все связанные данные.
     * </p>
     *
     * @param limit максимальное количество фильмов для возврата
     * @return коллекция из {@code limit} наиболее популярных фильмов
     */
    @Override
    public Collection<Film> findPopular(Integer limit) {
        return findMany(FIND_POPULAR_QUERY, limit).stream()
                .map(film -> {
                    film.setLikes(getFilmLikes(film.getId()));
                    film.setGenres(getFilmGenres(film.getId()));
                    film.setMpa(getMpa(film));
                    return film;
                })
                .toList();
    }

    /**
     * Получает множество идентификаторов пользователей, поставивших лайк фильму.
     *
     * @param id идентификатор фильма
     * @return множество ID пользователей, лайкнувших фильм
     */
    private Set<Long> getFilmLikes(Long id) {
        return jdbc.queryForList(LIKES_FILM_QUERY, id)
                .stream()
                .map(x -> ((Number) x.get("user_id")).longValue())
                .collect(Collectors.toSet());
    }

    /**
     * Получает множество жанров фильма с сортировкой по ID.
     *
     * @param filmId идентификатор фильма
     * @return отсортированное по ID множество жанров фильма
     */
    private Set<Genre> getFilmGenres(Long filmId) {
        return jdbc.queryForList(GENRES_FILM_QUERY, filmId)
                .stream()
                .map(x -> {
                    Genre genre = new Genre();
                    genre.setId((Integer) x.get("genre_id"));
                    genre.setName(x.get("name").toString());
                    return genre;
                })
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));
    }

    /**
     * Сохраняет связи фильма с его жанрами в таблицу film_genres.
     *
     * @param film фильм с указанными жанрами
     * @return множество сохраненных жанров
     */
    private Set<Genre> fillFilmGenres(Film film) {
        for (Genre genre : film.getGenres()) {
            jdbc.update(ADD_GENRE_TO_FILM, film.getId(), genre.getId());
        }
        return film.getGenres();
    }

    /**
     * Удаляет все связи фильма с жанрами.
     *
     * @param film фильм, для которого удаляются связи с жанрами
     */
    private void deleteFilmGenres(Film film) {
        jdbc.update(DELETE_GENRE_BY_FILM, film.getId());
    }

    /**
     * Получает полную информацию о рейтинге MPA фильма.
     *
     * @param film фильм, для которого запрашивается рейтинг
     * @return объект {@link Mpa} с полной информацией, или null если рейтинг не указан
     */
    private Mpa getMpa(Film film) {
        try {
            if (film.getMpa() == null) return null;
            return jdbc.queryForObject(FIND_MPA_BY_ID, new MpaRowMapper(), film.getMpa().getId());
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }
}