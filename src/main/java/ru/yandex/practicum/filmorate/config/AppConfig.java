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

/**
 * Конфигурационный класс приложения Filmorate.
 * <p>
 * Обеспечивает динамическое создание бинов-алиасов для различных типов хранилищ
 * на основе конфигурационных свойств. Позволяет гибко переключаться между
 * различными реализациями хранилищ (например, InMemory и Database) без изменения
 * кода сервисов.
 * </p>
 */
@Configuration
public class AppConfig {

    @Autowired
    private ApplicationContext context;

    /**
     * Создает алиас (псевдоним) для бина хранилища фильмов.
     * <p>
     * Метод динамически получает бин из контекста Spring по квалификатору,
     * указанному в свойстве {@code filmorate.storage.film}. Это позволяет
     * переключаться между реализациями {@link FilmStorage} (например,
     * {@code filmDbStorage} или {@code filmInMemoryStorage}) через
     * конфигурационный файл.
     * </p>
     *
     * @param qualifier квалификатор (имя бина) хранилища фильмов из конфигурации
     * @return экземпляр {@link FilmStorage} соответствующий указанному квалификатору
     * @see FilmStorage
     */
    @Bean
    public FilmStorage filmStorageAlias(@Value("${filmorate.storage.film}") String qualifier) {
        return (FilmStorage) context.getBean(qualifier);
    }

    /**
     * Создает алиас (псевдоним) для бина хранилища пользователей.
     * <p>
     * Метод динамически получает бин из контекста Spring по квалификатору,
     * указанному в свойстве {@code filmorate.storage.user}. Позволяет гибко
     * переключаться между реализациями {@link UserStorage} через конфигурацию.
     * </p>
     *
     * @param qualifier квалификатор (имя бина) хранилища пользователей из конфигурации
     * @return экземпляр {@link UserStorage} соответствующий указанному квалификатору
     * @see UserStorage
     */
    @Bean
    public UserStorage userStorageAlias(@Value("${filmorate.storage.user}") String qualifier) {
        return (UserStorage) context.getBean(qualifier);
    }

    /**
     * Создает алиас (псевдоним) для бина хранилища рейтингов MPA.
     * <p>
     * Метод динамически получает бин из контекста Spring по квалификатору,
     * указанному в свойстве {@code filmorate.storage.mpa}. Обеспечивает возможность
     * выбора реализации {@link MpaStorage} (например, работа с БД или in-memory)
     * через внешнюю конфигурацию.
     * </p>
     *
     * @param qualifier квалификатор (имя бина) хранилища рейтингов MPA из конфигурации
     * @return экземпляр {@link MpaStorage} соответствующий указанному квалификатору
     * @see MpaStorage
     */
    @Bean
    public MpaStorage mpaStorageAlias(@Value("${filmorate.storage.mpa}") String qualifier) {
        return (MpaStorage) context.getBean(qualifier);
    }

    /**
     * Создает алиас (псевдоним) для бина хранилища жанров.
     * <p>
     * Метод динамически получает бин из контекста Spring по квалификатору,
     * указанному в свойстве {@code filmorate.storage.genre}. Позволяет гибко
     * конфигурировать, какая реализация {@link GenreStorage} будет использоваться
     * в приложении (база данных или in-memory хранилище).
     * </p>
     *
     * @param qualifier квалификатор (имя бина) хранилища жанров из конфигурации
     * @return экземпляр {@link GenreStorage} соответствующий указанному квалификатору
     * @see GenreStorage
     */
    @Bean
    public GenreStorage genreStorageAlias(@Value("${filmorate.storage.genre}") String qualifier) {
        return (GenreStorage) context.getBean(qualifier);
    }
}