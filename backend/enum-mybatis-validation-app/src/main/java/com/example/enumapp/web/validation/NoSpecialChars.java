package com.example.enumapp.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * null/blank 는 통과.
 * 값이 있으면 문자(한글 포함)·숫자·공백·. _ - 만 허용하고 그 외 특수문자는 거부.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSpecialCharsValidator.class)
public @interface NoSpecialChars {

    String message() default "must not contain special characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
