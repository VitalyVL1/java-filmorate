package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Модель данных, представляющая пользователя приложения.
 * <p>
 * Содержит персональную информацию о пользователе и его социальные связи.
 * Используется для обмена данными между слоями приложения и для валидации
 * входных данных при регистрации и обновлении профиля.
 * </p>
 *
 * <p>Валидация полей:
 * <ul>
 *   <li>{@code email} — не может быть пустым, должен соответствовать формату email</li>
 *   <li>{@code login} — не может быть пустым, не должен содержать пробелы</li>
 *   <li>{@code birthday} — не может быть в будущем</li>
 *   <li>{@code name} — если не указано, при создании подставляется значение логина</li>
 * </ul>
 * </p>
 */
@Data
@Slf4j
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически при регистрации.
     */
    private Long id;

    /**
     * Карта дружеских связей пользователя.
     * <p>
     * Ключ — идентификатор друга, значение — {@link FriendStatus статус дружбы}.
     * Позволяет эффективно проверять наличие дружеских связей и их статус.
     * </p>
     */
    private Map<Long, FriendStatus> friends = new HashMap<>();

    /**
     * Электронная почта пользователя.
     * Обязательное поле, должно быть уникальным в системе и соответствовать
     * стандартному формату email (например, user@example.com).
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Введен некорректный email")
    private String email;

    /**
     * Логин пользователя для входа в систему.
     * Обязательное поле, должен быть уникальным и не содержать пробелов.
     * Используется как основное имя пользователя в системе, если не задано {@link #name}.
     */
    @NotBlank(message = "логин не может быть пустым")
    @Length(min = 1, message = "логин не может быть пустым")
    @Pattern(regexp = "^\\S*$", message = "логин не может содержать пробелы")
    private String login;

    /**
     * Отображаемое имя пользователя.
     * Необязательное поле. Если не указано при создании, автоматически
     * устанавливается равным {@link #login}.
     */
    private String name;

    /**
     * Дата рождения пользователя.
     * Формат: {@code yyyy-MM-dd}. Не может быть в будущем.
     * Используется для определения возраста и соответствующих прав доступа.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}