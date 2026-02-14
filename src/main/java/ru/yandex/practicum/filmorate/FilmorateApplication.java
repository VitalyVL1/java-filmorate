package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Filmorate.
 * <p>
 * Точка входа в Spring Boot приложение. Отвечает за запуск и конфигурацию
 * всего приложения. Аннотация {@link SpringBootApplication} включает в себя:
 * <ul>
 *   <li>{@code @Configuration} — помечает класс как источник конфигурации Spring</li>
 *   <li>{@code @EnableAutoConfiguration} — включает автоматическую конфигурацию Spring Boot</li>
 *   <li>{@code @ComponentScan} — включает сканирование компонентов в пакете
 *       {@code ru.yandex.practicum.filmorate} и всех подпакетах</li>
 * </ul>
 * </p>
 *
 * <p>При запуске метод {@link #main(String[])} инициализирует Spring контекст,
 * запускает встроенный веб-сервер (по умолчанию Tomcat) и делает приложение
 * доступным для обработки HTTP-запросов на порту 8080.</p>
 *
 * <p>Класс не требует модификации в процессе разработки и служит исключительно
 * для запуска приложения.</p>
 *
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class FilmorateApplication {

    /**
     * Точка входа в приложение.
     * <p>
     * Вызывает {@link SpringApplication#run(Class, String[])} для запуска
     * Spring Boot приложения с указанным главным классом и аргументами
     * командной строки.
     * </p>
     *
     * @param args аргументы командной строки, переданные при запуске приложения
     */
    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
    }
}