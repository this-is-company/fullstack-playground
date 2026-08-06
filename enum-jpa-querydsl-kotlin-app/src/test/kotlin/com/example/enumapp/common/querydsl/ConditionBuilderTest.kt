package com.example.enumapp.common.querydsl

import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.core.types.dsl.StringPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConditionBuilderTest {

    private val condition = ConditionBuilder.create()

    @Test
    fun returnsNull_whenValueMissing() {
        assertThat(condition.eq(NAME, null)).isNull()
        assertThat(condition.like(NAME, "  ")).isNull()
        assertThat(condition.containsIgnoreCase(NAME, null)).isNull()
        assertThat(condition.goe(QTY, null)).isNull()
        assertThat(condition.`in`(ID, null)).isNull()
        assertThat(condition.`in`(ID, emptyList())).isNull()
        assertThat(condition.between(QTY, null, null)).isNull()
        assertThat(condition.range(QTY, null, null, true)).isNull()
    }

    @Test
    fun returnsPredicate_whenValuePresent() {
        assertThat(condition.eq(NAME, "Kim")).isNotNull()
        assertThat(condition.like(NAME, "Kim")).isNotNull()
        assertThat(condition.containsIgnoreCase(NAME, "kim")).isNotNull()
        assertThat(condition.`in`(ID, listOf(1L, 2L))).isNotNull()
        assertThat(condition.goe(QTY, 5)).isNotNull()
    }

    @Test
    fun betweenAndRange() {
        val both: Predicate? = condition.between(QTY, 1, 10)
        assertThat(both).isNotNull()
        assertThat(both!!.toString().lowercase()).contains("between")

        assertThat(condition.between(QTY, 3, null)).isNotNull()
        assertThat(condition.between(QTY, null, 9)).isNotNull()

        val inclusive = condition.range(QTY, 1, 10, true)
        val exclusive = condition.range(QTY, 1, 10, false)
        assertThat(inclusive!!.toString()).isNotEqualTo(exclusive!!.toString())
    }

    companion object {
        private val NAME: StringPath = Expressions.stringPath("name")
        private val QTY: NumberPath<Int> = Expressions.numberPath(Int::class.javaObjectType, "qty")
        private val ID: NumberPath<Long> = Expressions.numberPath(Long::class.javaObjectType, "id")
    }
}
