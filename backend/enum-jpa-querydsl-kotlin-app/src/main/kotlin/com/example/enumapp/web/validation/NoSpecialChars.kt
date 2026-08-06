package com.example.enumapp.web.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * null/blank 는 통과.
 * 값이 있으면 문자(한글 포함)·숫자·공백·. _ - 만 허용하고 그 외 특수문자는 거부.
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [NoSpecialCharsValidator::class])
annotation class NoSpecialChars(
    val message: String = "must not contain special characters",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
