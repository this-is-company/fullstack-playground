package com.example.enumapp.web.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * null/blank 는 통과. 값이 있으면 yyyy-MM-dd 이고 실제 LocalDate 로 파싱 가능해야 함.
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [LocalDateStringValidator::class])
annotation class LocalDateString(
    val message: String = "must match LocalDate pattern yyyy-MM-dd",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
