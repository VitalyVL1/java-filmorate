package ru.yandex.practicum.filmorate.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
public class FilmUtil {
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
