package com.example.enumapp.common.jpa;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

class ConditionBuilderTest {

    @Test
    void eq_null_skipsPredicate() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Expression<String> path = mock(Expression.class);

        assertThat(ConditionBuilder.create(cb).eq(path, null)).isNull();
        verifyNoInteractions(cb);
    }

    @Test
    void like_blank_skipsPredicate() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Expression<String> path = mock(Expression.class);

        assertThat(ConditionBuilder.create(cb).like(path, "  ")).isNull();
        verifyNoInteractions(cb);
    }

    @Test
    void and_allNull_usesConjunction() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conjunction);

        assertThat(ConditionBuilder.create(cb).and(null, null)).isSameAs(conjunction);
        verify(cb).conjunction();
    }

    @Test
    void like_present_buildsLowerLike() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Expression<String> path = mock(Expression.class);
        @SuppressWarnings("unchecked")
        Expression<String> lower = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);
        when(cb.lower(path)).thenReturn(lower);
        when(cb.like(any(), anyString())).thenReturn(predicate);

        assertThat(ConditionBuilder.create(cb).like(path, "Kim")).isSameAs(predicate);
        verify(cb).like(lower, "%kim%");
    }
}
