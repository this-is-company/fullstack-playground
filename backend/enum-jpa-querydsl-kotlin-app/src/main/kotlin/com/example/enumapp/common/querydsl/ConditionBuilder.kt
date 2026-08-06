package com.example.enumapp.common.querydsl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.ComparableExpression
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.core.types.dsl.StringExpression

/**
 * QueryDSL `where(...)` 에 바로 넣을 수 있는 null-safe 조건 팩토리.
 * 값이 없으면 `null` 을 반환하며, QueryDSL 은 null predicate 를 무시한다.
 */
class ConditionBuilder private constructor() {

    fun <T> eq(path: SimpleExpression<T>, value: T?): Predicate? =
        if (value == null) null else path.eq(value)

    /** 부분 일치 (대소문자 무시) */
    fun containsIgnoreCase(path: StringExpression, value: String?): Predicate? =
        if (hasText(value)) path.containsIgnoreCase(value!!.trim()) else null

    /** LIKE %value% */
    fun like(path: StringExpression, value: String?): Predicate? =
        if (hasText(value)) path.like("%${value!!.trim()}%") else null

    /** between. 한쪽만 있으면 goe/loe */
    fun <T> between(path: ComparableExpression<T>, from: T?, to: T?): Predicate?
        where T : Comparable<*> {
        if (from != null && to != null) {
            return path.between(from, to)
        }
        if (from != null) {
            return path.goe(from)
        }
        if (to != null) {
            return path.loe(to)
        }
        return null
    }

    fun <T> between(path: NumberExpression<T>, from: T?, to: T?): Predicate?
        where T : Number, T : Comparable<*> {
        if (from != null && to != null) {
            return path.between(from, to)
        }
        if (from != null) {
            return path.goe(from)
        }
        if (to != null) {
            return path.loe(to)
        }
        return null
    }

    /** toInclusive=false 이면 to 는 exclusive (<) */
    fun <T> range(
        path: ComparableExpression<T>,
        from: T?,
        to: T?,
        toInclusive: Boolean
    ): Predicate? where T : Comparable<*> {
        val builder = BooleanBuilder()
        if (from != null) {
            builder.and(path.goe(from))
        }
        if (to != null) {
            builder.and(if (toInclusive) path.loe(to) else path.lt(to))
        }
        return if (builder.hasValue()) builder else null
    }

    fun <T> range(
        path: NumberExpression<T>,
        from: T?,
        to: T?,
        toInclusive: Boolean
    ): Predicate? where T : Number, T : Comparable<*> {
        val builder = BooleanBuilder()
        if (from != null) {
            builder.and(path.goe(from))
        }
        if (to != null) {
            builder.and(if (toInclusive) path.loe(to) else path.lt(to))
        }
        return if (builder.hasValue()) builder else null
    }

    fun <T> `in`(path: SimpleExpression<T>, values: Collection<out T>?): Predicate? =
        if (values.isNullOrEmpty()) null else path.`in`(values)

    fun <T> goe(path: NumberExpression<T>, value: T?): Predicate?
        where T : Number, T : Comparable<*> =
        if (value == null) null else path.goe(value)

    fun <T> loe(path: NumberExpression<T>, value: T?): Predicate?
        where T : Number, T : Comparable<*> =
        if (value == null) null else path.loe(value)

    fun <T> goe(path: ComparableExpression<T>, value: T?): Predicate?
        where T : Comparable<*> =
        if (value == null) null else path.goe(value)

    fun <T> loe(path: ComparableExpression<T>, value: T?): Predicate?
        where T : Comparable<*> =
        if (value == null) null else path.loe(value)

    companion object {
        private val INSTANCE = ConditionBuilder()

        @JvmStatic
        fun create(): ConditionBuilder = INSTANCE

        private fun hasText(value: String?): Boolean =
            value != null && value.isNotBlank()
    }
}
