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

@Data
@Slf4j
public class User {
    private Long id;
    private Map<Long, FriendStatus> friends = new HashMap<>();

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Введен некорректный email")
    private String email;

    @NotBlank(message = "логин не может быть пустым")
    @Length(min = 1, message = "логин не может быть пустым")
    @Pattern(regexp = "^\\S*$", message = "логин не может содержать пробелы")
    private String login;

    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
