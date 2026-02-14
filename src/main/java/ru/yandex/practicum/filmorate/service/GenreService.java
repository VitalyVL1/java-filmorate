package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;

import java.util.Collection;

/**
 * Сервисный слой для управления жанрами фильмов.
 * <p>
 * Реализует бизнес-логику работы со справочником жанров. Несмотря на то,
 * что в приложении жанры являются предопределенным справочником (1-6),
 * сервис предоставляет полный CRUD функционал для возможного расширения
 * и администрирования жанров.
 * </p>
 *
 * <p>Основные бизнес-правила:
 * <ul>
 *   <li>Название жанра должно быть уникальным</li>
 *   <li>Нельзя создать жанр с существующим названием</li>
 *   <li>При обновлении проверяется уникальность названия, если оно изменяется</li>
 * </ul>
 * </p>
 */
@Service
public class GenreService {
    private final GenreStorage genreStorage;

    /**
     * Конструктор сервиса жанров.
     *
     * @param genreStorage хранилище жанров (алиас из {@link ru.yandex.practicum.filmorate.config.AppConfig})
     */
    public GenreService(@Qualifier("genreStorageAlias") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    /**
     * Создает новый жанр.
     * <p>
     * Перед созданием проверяет уникальность названия жанра.
     * </p>
     *
     * @param genre объект жанра для создания
     * @return созданный жанр с автоматически сгенерированным идентификатором
     * @throws DuplicatedDataException если жанр с таким названием уже существует
     */
    public Genre create(Genre genre) {
        if (containsName(genre)) {
            throw new DuplicatedDataException("Такой жанр уже существует");
        }
        return genreStorage.create(genre);
    }

    /**
     * Находит жанр по его идентификатору.
     *
     * @param id идентификатор жанра
     * @return найденный жанр
     * @throws ConditionsNotMetException если id равен null
     * @throws NotFoundException если жанр с указанным id не найден
     */
    public Genre findById(Integer id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }

    /**
     * Возвращает список всех жанров.
     *
     * @return коллекция всех жанров в системе
     */
    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    /**
     * Обновляет существующий жанр.
     * <p>
     * Проверяет существование жанра и уникальность названия при его изменении.
     * </p>
     *
     * @param newGenre объект жанра с обновленными данными
     * @return обновленный жанр
     * @throws ConditionsNotMetException если id не указан
     * @throws NotFoundException если жанр с указанным id не найден
     * @throws DuplicatedDataException если новое название уже существует у другого жанра
     */
    public Genre update(Genre newGenre) {
        Genre oldGenre = findById(newGenre.getId());

        if (newGenre.getName() != null &&
            !newGenre.getName().equals(oldGenre.getName()) &&
            containsName(newGenre)) {
            throw new DuplicatedDataException("Такой жанр уже существует");
        }

        return genreStorage.update(newGenre)
                .orElseThrow(
                        () -> new NotFoundException("Жанр с id = " + newGenre.getId() + " не найден")
                );
    }

    /**
     * Проверяет, существует ли жанр с таким названием.
     * <p>
     * Вспомогательный метод для проверки уникальности названия при создании и обновлении.
     * </p>
     *
     * @param genre жанр с проверяемым названием
     * @return {@code true} если жанр с таким названием уже существует, иначе {@code false}
     */
    private boolean containsName(Genre genre) {
        return genreStorage.findAll().stream()
                .map(Genre::getName)
                .anyMatch(genre.getName()::equals);
    }

    /**
     * Проверяет, существует ли такой жанр в системе.
     * <p>
     * Используется внешними сервисами (например, {@link FilmService}) для валидации
     * жанров, привязанных к фильмам.
     * </p>
     *
     * @param genre жанр для проверки (сравнение по всем полям через {@link Genre#equals})
     * @return {@code true} если такой жанр существует, иначе {@code false}
     * @see FilmService#checkMpaAndGenre(ru.yandex.practicum.filmorate.model.Film)
     */
    public boolean containsGenre(Genre genre) {
        return genreStorage.findAll().stream()
                .anyMatch(genre::equals);
    }
}