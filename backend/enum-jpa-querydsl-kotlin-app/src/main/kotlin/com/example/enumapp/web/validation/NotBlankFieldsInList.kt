package com.example.enumapp.web.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * List 자체가 null/empty 이면 오류.
 * 요소의 지정 필드가 null 또는 blank("") 이면 오류.
 */
@MustBeDocumented
@Target(FIELD, VALUE_PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = [NotBlankFieldsInListValidator::class])
annotation class NotBlankFieldsInList(
    val message: String = "list items contain null or blank required fields",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    /** 검사할 필드명 (getter 기준 bean property) */
    val fields: Array<String>,
)
