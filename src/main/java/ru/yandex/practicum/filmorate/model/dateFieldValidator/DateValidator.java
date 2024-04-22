package ru.yandex.practicum.filmorate.model.dateFieldValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<IsAfter, LocalDate> {

    private String validDate;

    @Override
    public void initialize(IsAfter constraintAnnotation) {
        validDate = constraintAnnotation.current();
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        String[] splitDate = validDate.split("-");
        return date.isAfter(LocalDate.of(Integer.parseInt(splitDate[0]),
                Integer.parseInt(splitDate[1]),
                Integer.parseInt(splitDate[2])));
    }
}