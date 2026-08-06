package com.example.enumapp.web.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.NotEmpty
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/** GET `@RequestParam List` 용 — null/empty 금지. */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [])
@NotEmpty
@ReportAsSingleViolation
annotation class NotEmptyList(
    val message: String = "list must not be null or empty",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
