package com.example.enumapp.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * null/blank 는 통과. 값이 있으면 yyyy-MM-dd'T'HH:mm:ss 이고 실제 LocalDateTime 으로 파싱 가능해야 함.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LocalDateTimeStringValidator.class)
public @interface LocalDateTimeString {

    String message() default "must match LocalDateTime pattern yyyy-MM-dd'T'HH:mm:ss";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
