package ru.yandex.practicum.filmorate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Configuration
public class AppConfig {
    @Autowired
    private ApplicationContext context;

    @Bean
    public FilmStorage filmStorageAlias(@Value("${filmorate.storage.film}") String qualifier) {
        return (FilmStorage) context.getBean(qualifier);
    }

    @Bean
    public UserStorage userStorageAlias(@Value("${filmorate.storage.user}") String qualifier) {
        return (UserStorage) context.getBean(qualifier);
    }

    @Bean
    public MpaStorage mpaStorageAlias(@Value("${filmorate.storage.mpa}") String qualifier) {
        return (MpaStorage) context.getBean(qualifier);
    }

    @Bean
    public GenreStorage genreStorageAlias(@Value("${filmorate.storage.genre}") String qualifier) {
        return (GenreStorage) context.getBean(qualifier);
    }
}
