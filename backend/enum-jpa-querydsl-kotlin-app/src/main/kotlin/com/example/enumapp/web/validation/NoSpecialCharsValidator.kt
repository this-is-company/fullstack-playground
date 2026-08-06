package com.example.enumapp.web.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.regex.Pattern

class NoSpecialCharsValidator : ConstraintValidator<NoSpecialChars, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null || value.isBlank()) {
            return true
        }
        return ALLOWED.matcher(value).matches()
    }

    companion object {
        /** 유니코드 문자·숫자·공백·. _ - 만 허용 */
        private val ALLOWED: Pattern = Pattern.compile("^[\\p{L}\\p{N}\\s._-]*$")
    }
}
