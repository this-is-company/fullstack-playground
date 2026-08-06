package com.example.enumapp.web.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/** List 자체는 허용하되, 요소 중 null 이 있으면 실패. */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [NoNullElementsValidator::class])
annotation class NoNullElements(
    val message: String = "list must not contain null elements",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
