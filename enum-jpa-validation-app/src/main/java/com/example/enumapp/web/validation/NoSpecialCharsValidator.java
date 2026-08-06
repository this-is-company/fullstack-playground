package com.example.enumapp.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NoSpecialCharsValidator implements ConstraintValidator<NoSpecialChars, String> {

    /** 유니코드 문자·숫자·공백·. _ - 만 허용 */
    private static final Pattern ALLOWED = Pattern.compile("^[\\p{L}\\p{N}\\s._-]*$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return ALLOWED.matcher(value).matches();
    }
}
