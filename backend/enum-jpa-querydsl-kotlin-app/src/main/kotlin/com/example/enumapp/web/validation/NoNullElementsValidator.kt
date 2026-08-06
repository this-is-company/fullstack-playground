package com.example.enumapp.web.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NoNullElementsValidator : ConstraintValidator<NoNullElements, Collection<*>> {

    override fun isValid(value: Collection<*>?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true // null list 는 @NotNull / @NotEmpty 가 담당
        }
        return value.none { it == null }
    }
}
