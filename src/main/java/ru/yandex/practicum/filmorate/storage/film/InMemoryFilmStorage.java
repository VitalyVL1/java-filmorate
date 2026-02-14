package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genres.InMemoryGenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.InMemoryMpaStorage;
import ru.yandex.practicum.filmorate.util.FilmUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация хранилища фильмов на основе оперативной памяти (in-memory).
 * <p>
 * Обеспечивает временное хранение данных о фильмах, используя {@link HashMap}
 * для хранения объектов. Предназначен для разработки и тестирования, не
 * обеспечивает персистентность данных между перезапусками приложения.
 * </p>
 *
 * <p>Основные особенности:
 * <ul>
 *   <li>Хранение всех данных в оперативной памяти</li>
 *   <li>Использование вспомогательных in-memory хранилищ для жанров и MPA</li>
 *   <li>Автоматическая генерация идентификаторов</li>
 *   <li>Сортировка жанров по ID при сохранении</li>
 * </ul>
 * </p>
 *
 * <p><b>Важно:</b> Данная реализация не подходит для production-среды,
 * так как все данные теряются при остановке приложения. Используется
 * на этапе разработки для быстрого прототипирования.</p>
 *
 * @see ru.yandex.practicum.filmorate.storage.film.FilmStorage
 * @see ru.yandex.practicum.filmorate.storage.film.FilmDbStorage
 */
@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    /**
     * Хранилище фильмов в памяти.
     * Ключ - идентификатор фильма, значение - объект фильма.
     */
    private final Map<Long, Film> films = new HashMap<>();

    /**
     * Вспомогательное in-memory хранилище для жанров.
     * Используется для получения полной информации о жанрах по ID.
     */
    private final InMemoryGenreStorage inMemoryGenreStorage = new InMemoryGenreStorage();

    /**
     * Вспомогательное in-memory хранилище для рейтингов MPA.
     * Используется для получения полной информации о рейтингах по ID.
     */
    private final InMemoryMpaStorage inMemoryMpaStorage = new InMemoryMpaStorage();

    /**
     * Создает новый фильм в in-memory хранилище.
     * <p>
     * Генерирует новый идентификатор для фильма. Если в фильме указаны MPA или жанры,
     * загружает полную информацию о них из соответствующих in-memory хранилищ.
     * </p>
     *
     * @param film объект фильма для сохранения
     * @return сохраненный фильм с заполненными полями
     * @throws NotFoundException если указанный MPA или жанр не найдены
     */
    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        if (film.getMpa() != null) {
            film.setMpa(getMpa(film));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.setGenres(getGenres(film));
        }

        films.put(film.getId(), film);
        return films.get(film.getId());
    }

    /**
     * Находит фильм по его идентификатору.
     *
     * @param id идентификатор фильма
     * @return {@link Optional} с найденным фильмом, или пустой {@link Optional},
     *         если фильм не найден
     */
    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    /**
     * Возвращает список всех фильмов.
     *
     * @return коллекция всех фильмов в памяти
     */
    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    /**
     * Обновляет существующий фильм.
     * <p>
     * Если фильм с указанным ID не существует, возвращает пустой Optional.
     * При обновлении используется утилитный метод {@link FilmUtil#filmFieldsUpdate}
     * для копирования полей. Если в обновляемом фильме указаны новые MPA или жанры,
     * загружает полную информацию о них.
     * </p>
     *
     * @param newFilm объект фильма с обновленными данными
     * @return {@link Optional} с обновленным фильмом, или пустой {@link Optional},
     *         если фильм не найден
     */
    @Override
    public Optional<Film> update(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            return Optional.empty();
        }

        Film updatedFilm = FilmUtil.filmFieldsUpdate(films.get(newFilm.getId()), newFilm);

        if (newFilm.getMpa() != null) {
            updatedFilm.setMpa(getMpa(newFilm));
        }

        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            updatedFilm.setGenres(newFilm.getGenres());
        }

        films.put(updatedFilm.getId(), updatedFilm);

        return Optional.of(films.get(newFilm.getId()));
    }

    /**
     * Удаляет фильм по идентификатору.
     *
     * @param id идентификатор фильма
     * @return {@link Optional} с удаленным фильмом, или пустой {@link Optional},
     *         если фильм не найден
     */
    @Override
    public Optional<Film> removeById(Long id) {
        return Optional.ofNullable(films.remove(id));
    }

    /**
     * Добавляет лайк фильму от пользователя.
     * <p>
     * Обновляет множество лайков у фильма и сохраняет изменения в мапе.
     * </p>
     *
     * @param film фильм, которому ставится лайк
     * @param user пользователь, ставящий лайк
     * @return обновленный фильм
     */
    @Override
    public Film addLike(Film film, User user) {
        film.getLikes().add(user.getId());
        films.put(film.getId(), film);
        return film;
    }

    /**
     * Удаляет лайк пользователя с фильма.
     * <p>
     * Обновляет множество лайков у фильма и сохраняет изменения в мапе.
     * </p>
     *
     * @param film фильм, с которого удаляется лайк
     * @param user пользователь, удаляющий лайк
     * @return обновленный фильм
     */
    @Override
    public Film removeLike(Film film, User user) {
        film.getLikes().remove(user.getId());
        films.put(film.getId(), film);
        return film;
    }

    /**
     * Возвращает список наиболее популярных фильмов.
     * <p>
     * Фильмы сортируются по убыванию количества лайков.
     * При равенстве количества лайков порядок не гарантируется.
     * </p>
     *
     * @param limit максимальное количество фильмов для возврата
     * @return коллекция из {@code limit} наиболее популярных фильмов
     */
    @Override
    public Collection<Film> findPopular(Integer limit) {
        Comparator<Film> comparator = Comparator.comparingInt(f -> f.getLikes().size());
        return films.values()
                .stream()
                .sorted(comparator.reversed())
                .limit(limit)
                .toList();
    }

    /**
     * Генерирует следующий уникальный идентификатор для фильма.
     * <p>
     * Находит максимальный существующий ID и увеличивает его на 1.
     * Если хранилище пусто, возвращает 1.
     * </p>
     *
     * @return следующий уникальный идентификатор
     */
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .reduce(Long::max)
                .orElse(0L);
        return ++currentMaxId;
    }

    /**
     * Получает полную информацию о рейтинге MPA из in-memory хранилища.
     *
     * @param film фильм, содержащий ID рейтинга
     * @return объект {@link Mpa} с полной информацией
     * @throws NotFoundException если рейтинг с указанным ID не найден
     */
    private Mpa getMpa(Film film) {
        return inMemoryMpaStorage.findById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Mpa не найден, указанный id = " + film.getMpa().getId()));
    }

    /**
     * Получает полную информацию о жанрах из in-memory хранилища.
     * <p>
     * Для каждого ID жанра из фильма загружает полный объект {@link Genre}
     * из хранилища жанров. Результат сортируется по ID жанра.
     * </p>
     *
     * @param film фильм, содержащий ID жанров
     * @return отсортированное по ID множество жанров
     * @throws NotFoundException если какой-либо жанр не найден
     */
    private Set<Genre> getGenres(Film film) {
        return film.getGenres().stream().map(
                        genre -> inMemoryGenreStorage.findById(genre.getId())
                                .orElseThrow(() -> new NotFoundException("Genre не найден, указанный id = " + genre.getId())))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(Genre::getId))));
    }
}