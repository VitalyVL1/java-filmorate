package ru.yandex.practicum.filmorate.exception_handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ErrorResponse {
    private String message;
}
