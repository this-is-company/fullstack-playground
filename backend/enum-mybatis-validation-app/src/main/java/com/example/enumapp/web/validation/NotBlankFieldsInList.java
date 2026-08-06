package com.example.enumapp.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List 자체가 null/empty 이면 오류.
 * 요소의 지정 필드가 null 또는 blank("") 이면 오류.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankFieldsInListValidator.class)
public @interface NotBlankFieldsInList {

    String message() default "list items contain null or blank required fields";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** 검사할 필드명 (getter 기준 bean property) */
    String[] fields();
}
