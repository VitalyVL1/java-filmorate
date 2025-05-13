package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.validator.constraints.DateAfter;

import java.time.LocalDate;

public class DateAfterValidator implements ConstraintValidator<DateAfter, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(DateAfter constraintAnnotation) {
        minDate = LocalDate.parse(constraintAnnotation.minDate());
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        return localDate != null && localDate.isAfter(minDate);
    }
}
