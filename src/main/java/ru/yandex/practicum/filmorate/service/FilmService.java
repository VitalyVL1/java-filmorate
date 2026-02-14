package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

/**
 * Сервисный слой для управления фильмами.
 * <p>
 * Реализует бизнес-логику работы с фильмами, включая операции создания,
 * обновления, удаления, поиска, а также функционал лайков и получения
 * популярных фильмов. Выступает прослойкой между контроллерами и хранилищами,
 * обеспечивая валидацию данных и проверку бизнес-правил.
 * </p>
 *
 * <p>Основные бизнес-правила:
 * <ul>
 *   <li>Нельзя создать/обновить фильм с несуществующим MPA или жанром</li>
 *   <li>Нельзя поставить лайк от несуществующего пользователя</li>
 *   <li>Все операции с конкретным фильмом требуют проверки его существования</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    /**
     * Конструктор сервиса фильмов.
     * <p>
     * Использует квалификаторы-алиасы из {@link ru.yandex.practicum.filmorate.config.AppConfig}
     * для внедрения соответствующих реализаций хранилищ.
     * </p>
     *
     * @param filmStorage хранилище фильмов
     * @param userStorage хранилище пользователей (для проверки при лайках)
     * @param mpaStorage хранилище рейтингов MPA (для проверки существования)
     * @param genreStorage хранилище жанров (для проверки существования)
     */
    public FilmService(
            @Qualifier("filmStorageAlias") FilmStorage filmStorage,
            @Qualifier("userStorageAlias") UserStorage userStorage,
            @Qualifier("mpaStorageAlias") MpaStorage mpaStorage,
            @Qualifier("genreStorageAlias") GenreStorage genreStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    /**
     * Создает новый фильм.
     * <p>
     * Перед созданием проверяет существование указанных MPA и жанров.
     * </p>
     *
     * @param film объект фильма для создания
     * @return созданный фильм с автоматически сгенерированным идентификатором
     * @throws NotFoundException если указанный MPA или жанр не существуют
     */
    public Film create(Film film) {
        checkMpaAndGenre(film);
        return filmStorage.create(film);
    }

    /**
     * Находит фильм по его идентификатору.
     *
     * @param id идентификатор фильма
     * @return найденный фильм
     * @throws ConditionsNotMetException если id равен null
     * @throws NotFoundException если фильм с указанным id не найден
     */
    public Film findById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    /**
     * Возвращает список всех фильмов.
     *
     * @return коллекция всех фильмов в системе
     */
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    /**
     * Обновляет существующий фильм.
     * <p>
     * Перед обновлением проверяет наличие id, существование фильма,
     * а также существование указанных MPA и жанров.
     * </p>
     *
     * @param newFilm объект фильма с обновленными данными
     * @return обновленный фильм
     * @throws ConditionsNotMetException если id не указан
     * @throws NotFoundException если фильм, MPA или жанр не найдены
     */
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        checkMpaAndGenre(newFilm);

        return filmStorage.update(newFilm)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден"));
    }

    /**
     * Удаляет фильм по идентификатору.
     *
     * @param id идентификатор фильма
     * @return удаленный фильм
     * @throws ConditionsNotMetException если id равен null
     * @throws NotFoundException если фильм с указанным id не найден
     */
    public Film removeById(Long id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return filmStorage.removeById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    /**
     * Добавляет лайк фильму от пользователя.
     *
     * @param id идентификатор фильма
     * @param userId идентификатор пользователя
     * @return обновленный фильм с информацией о лайках
     * @throws NotFoundException если фильм или пользователь не найдены
     */
    public Film addLike(Long id, Long userId) {
        return filmStorage.addLike(findById(id), getUser(userId));
    }

    /**
     * Удаляет лайк пользователя с фильма.
     *
     * @param id идентификатор фильма
     * @param userId идентификатор пользователя
     * @return обновленный фильм с информацией о лайках
     * @throws NotFoundException если фильм, пользователь или лайк не найдены
     */
    public Film removeLike(Long id, Long userId) {
        return filmStorage.removeLike(findById(id), getUser(userId));
    }

    /**
     * Возвращает список популярных фильмов.
     * <p>
     * Фильмы сортируются по количеству лайков. При равенстве лайков
     * порядок не гарантируется (зависит от реализации хранилища).
     * </p>
     *
     * @param limit максимальное количество фильмов для возврата
     * @return коллекция из {@code limit} наиболее популярных фильмов
     */
    public Collection<Film> findPopular(Integer limit) {
        return filmStorage.findPopular(limit);
    }

    /**
     * Вспомогательный метод для получения пользователя по ID с проверкой существования.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь
     * @throws NotFoundException если пользователь не найден
     */
    private User getUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь с id = " + id + " не найден")
                );
    }

    /**
     * Проверяет существование MPA и жанров, указанных в фильме.
     * <p>
     * Если в фильме указан MPA или жанры, проверяет их наличие в соответствующих
     * хранилищах. При отсутствии любого из элементов выбрасывает исключение.
     * </p>
     *
     * @param film фильм для проверки
     * @throws NotFoundException если MPA или любой из жанров не найдены
     */
    private void checkMpaAndGenre(Film film) {
        if (film.getMpa() != null && !mpaStorage.contains(film.getMpa())) {
            throw new NotFoundException("Mpa не найден, указанный id = " + film.getMpa().getId());
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreStorage.contains(genre)) {
                    throw new NotFoundException("Genre не найден, указанный id = " + genre.getId());
                }
            }
        }
    }
}