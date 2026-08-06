package com.example.enumapp.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** List 자체는 허용하되, 요소 중 null 이 있으면 실패. */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoNullElementsValidator.class)
public @interface NoNullElements {

    String message() default "list must not contain null elements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
