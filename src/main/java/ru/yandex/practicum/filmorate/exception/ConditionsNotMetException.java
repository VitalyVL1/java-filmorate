package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ValidationException;

public class ConditionsNotMetException extends ValidationException {
    public ConditionsNotMetException(String message) {
        super(message);
    }
}
