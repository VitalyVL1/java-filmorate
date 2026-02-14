package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Map;
import java.util.TreeMap;

/**
 * Утилитный класс для инициализации справочника жанров.
 * <p>
 * Предоставляет методы для создания предопределенного набора жанров,
 * используемых в приложении. Жанры являются неизменяемым справочником
 * и соответствуют стандартной классификации фильмов.
 * </p>
 *
 * <p>Создаваемые жанры (с соответствующими ID):
 * <ul>
 *   <li>1 - Комедия</li>
 *   <li>2 - Драма</li>
 *   <li>3 - Мультфильм</li>
 *   <li>4 - Триллер</li>
 *   <li>5 - Документальный</li>
 *   <li>6 - Боевик</li>
 * </ul>
 * </p>
 *
 * <p>Жанры хранятся в {@link TreeMap} с сортировкой по ключу (ID),
 * что гарантирует упорядоченность при итерации.</p>
 *
 * @see ru.yandex.practicum.filmorate.model.Genre
 * @see ru.yandex.practicum.filmorate.storage.genres.InMemoryGenreStorage
 */
public class GenreUtil {

    /**
     * Создает и заполняет карту предопределенных жанров.
     * <p>
     * Метод создает объекты жанров с ID от 1 до 6 и соответствующими
     * названиями из предопределенного массива. Жанры помещаются в {@link TreeMap},
     * где ключом является ID жанра, что обеспечивает естественную сортировку
     * при последующем использовании.
     * </p>
     *
     * <p>Процесс создания:
     * <ol>
     *   <li>Создается пустая {@link TreeMap} с компаратором по Integer</li>
     *   <li>Для каждого индекса от 1 до длины массива названий:
     *       <ul>
     *           <li>Создается новый объект {@link Genre}</li>
     *           <li>Устанавливается ID равный текущему индексу</li>
     *           <li>Устанавливается название из массива genreNames</li>
     *           <li>Жанр помещается в карту под своим ID</li>
     *       </ul>
     *   </li>
     * </ol>
     * </p>
     *
     * <p><b>Важно:</b> Данный метод используется для инициализации
     * in-memory хранилища жанров при старте приложения и гарантирует
     * наличие базового набора жанров.</p>
     *
     * @return {@link Map} с ключом - ID жанра (Integer) и значением - объект {@link Genre},
     *         содержащая все предопределенные жанры, отсортированные по ID
     *
     * @see java.util.TreeMap
     * @see ru.yandex.practicum.filmorate.storage.genres.InMemoryGenreStorage
     */
    public static Map<Integer, Genre> fillGenres() {
        Map<Integer, Genre> genreMap = new TreeMap<>(Integer::compareTo);
        String[] genreNames = {
                "Комедия",
                "Драма",
                "Мультфильм",
                "Триллер",
                "Документальный",
                "Боевик"
        };

        for (int i = 1; i <= genreNames.length; i++) {
            Genre genre = new Genre();
            genre.setId(i);
            genre.setName(genreNames[i - 1]);
            genreMap.put(genre.getId(), genre);
        }

        return genreMap;
    }
}