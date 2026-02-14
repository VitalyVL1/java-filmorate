package ru.yandex.practicum.filmorate.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;

/**
 * Утилитный класс для работы с объектами {@link Film}.
 * <p>
 * Содержит вспомогательные методы для манипуляции данными фильмов,
 * которые используются в различных частях приложения (сервисы, хранилища).
 * Все методы класса являются статическими и не требуют создания экземпляра класса.
 * </p>
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Обновление полей фильма с логированием изменений</li>
 *   <li>Копирование данных из одного объекта Film в другой</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.model.Film
 * @see ru.yandex.practicum.filmorate.service.FilmService
 * @see ru.yandex.practicum.filmorate.storage.film.FilmDbStorage
 * @see ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage
 */
@Slf4j
public class FilmUtil {

    /**
     * Обновляет поля существующего фильма значениями из нового фильма.
     * <p>
     * Метод проверяет каждое поле нового фильма на {@code null} и,
     * если поле не null, обновляет соответствующее поле старого фильма.
     * Все операции обновления логируются с указанием нового значения.
     * </p>
     *
     * <p>Обновляемые поля:
     * <ul>
     *   <li>{@link Film#name} — название фильма</li>
     *   <li>{@link Film#description} — описание</li>
     *   <li>{@link Film#releaseDate} — дата релиза</li>
     *   <li>{@link Film#duration} — продолжительность</li>
     *   <li>{@link Film#mpa} — рейтинг MPA</li>
     *   <li>{@link Film#genres} — множество жанров</li>
     * </ul>
     * </p>
     *
     * <p><b>Важно:</b> Метод не обновляет идентификатор фильма ({@link Film#id})
     * и множество лайков ({@link Film#likes}). Эти поля должны управляться
     * отдельно на уровне сервисов или хранилищ.</p>
     *
     * @param oldFilm существующий фильм, который нужно обновить
     * @param newFilm фильм с новыми значениями полей (может содержать null-поля)
     * @return обновленный существующий фильм (тот же объект, что и oldFilm)
     *
     * @see ru.yandex.practicum.filmorate.service.FilmService#update(Film)
     * @see ru.yandex.practicum.filmorate.storage.film.FilmDbStorage#update(Film)
     */
    public static Film filmFieldsUpdate(Film oldFilm, Film newFilm) {
        if (newFilm.getName() != null) {
            log.info("Updating film with name: {}", newFilm.getName());
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            log.info("Updating film with description: {}", newFilm.getDescription());
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            log.info("Updating film with releaseDate: {}", newFilm.getReleaseDate());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            log.info("Updating film with duration: {}", newFilm.getDuration());
            oldFilm.setDuration(newFilm.getDuration());
        }
        if (newFilm.getMpa() != null) {
            log.info("Updating film with mpa: {}", newFilm.getMpa());
            oldFilm.setMpa(newFilm.getMpa());
        }
        if (newFilm.getGenres() != null) {
            log.info("Updating film with genres: {}", newFilm.getGenres());
            oldFilm.setGenres(newFilm.getGenres());
        }
        return oldFilm;
    }
}