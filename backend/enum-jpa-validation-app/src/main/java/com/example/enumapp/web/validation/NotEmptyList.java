package com.example.enumapp.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotEmpty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** GET {@code @RequestParam List} 용 — null/empty 금지. */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotEmpty
@ReportAsSingleViolation
public @interface NotEmptyList {

    String message() default "list must not be null or empty";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
