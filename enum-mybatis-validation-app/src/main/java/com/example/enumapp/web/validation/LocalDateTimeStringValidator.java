package com.example.enumapp.web.validation;

import com.example.enumapp.common.time.DateFormats;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

public class LocalDateTimeStringValidator implements ConstraintValidator<LocalDateTimeString, String> {

    private static final Pattern FORMAT = Pattern.compile("^" + DateFormats.LOCAL_DATE_TIME + "$");
    private static final DateTimeFormatter PARSER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String trimmed = value.trim();
        if (!FORMAT.matcher(trimmed).matches()) {
            return false;
        }
        try {
            LocalDateTime.parse(trimmed, PARSER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
