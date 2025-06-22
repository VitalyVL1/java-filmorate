package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Map;
import java.util.TreeMap;

public class GenreUtil {
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
