package com.example.enumapp.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

public class NoNullElementsValidator implements ConstraintValidator<NoNullElements, Collection<?>> {

    @Override
    public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null list 는 @NotNull / @NotEmpty 가 담당
        }
        return value.stream().noneMatch(e -> e == null);
    }
}
