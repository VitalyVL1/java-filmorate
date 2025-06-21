package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> findAll() {
        log.debug("Find all films");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film findById(@PathVariable Long id) {
        log.debug("Find film by id {}", id);
        return filmService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Create film: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film update(@Valid @RequestBody Film newFilm) {
        log.debug("Update film: {}", newFilm);
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Like film: {} by user: {}", id, userId);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Film removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.debug("Delete like film: {} by user: {}", id, userId);
        return filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> findPopular(@RequestParam(defaultValue = "10") Integer limit) {
        log.debug("Find popular {} films", limit);
        return filmService.findPopular(limit);
    }
}
