package com.example.enumapp.common.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionBuilderTest {

    private static final StringPath NAME = Expressions.stringPath("name");
    private static final NumberPath<Integer> QTY = Expressions.numberPath(Integer.class, "qty");
    private static final NumberPath<Long> ID = Expressions.numberPath(Long.class, "id");

    private final ConditionBuilder condition = ConditionBuilder.create();

    @Test
    void returnsNull_whenValueMissing() {
        assertThat(condition.eq(NAME, null)).isNull();
        assertThat(condition.like(NAME, "  ")).isNull();
        assertThat(condition.containsIgnoreCase(NAME, null)).isNull();
        assertThat(condition.goe(QTY, null)).isNull();
        assertThat(condition.in(ID, null)).isNull();
        assertThat(condition.in(ID, List.of())).isNull();
        assertThat(condition.between(QTY, null, null)).isNull();
        assertThat(condition.range(QTY, null, null, true)).isNull();
    }

    @Test
    void returnsPredicate_whenValuePresent() {
        assertThat(condition.eq(NAME, "Kim")).isNotNull();
        assertThat(condition.like(NAME, "Kim")).isNotNull();
        assertThat(condition.containsIgnoreCase(NAME, "kim")).isNotNull();
        assertThat(condition.in(ID, List.of(1L, 2L))).isNotNull();
        assertThat(condition.goe(QTY, 5)).isNotNull();
    }

    @Test
    void betweenAndRange() {
        Predicate both = condition.between(QTY, 1, 10);
        assertThat(both).isNotNull();
        assertThat(both.toString().toLowerCase()).contains("between");

        assertThat(condition.between(QTY, 3, null)).isNotNull();
        assertThat(condition.between(QTY, null, 9)).isNotNull();

        Predicate inclusive = condition.range(QTY, 1, 10, true);
        Predicate exclusive = condition.range(QTY, 1, 10, false);
        assertThat(inclusive.toString()).isNotEqualTo(exclusive.toString());
    }
}
