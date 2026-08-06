package com.example.enumapp.common.mybatis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionBuilderTest {

    @Test
    void skipsNullValues_andBuildsWhereXml() {
        var condition = ConditionBuilder.create()
                .eq("o.status", "status", null)
                .like("o.customer_name", "customerName", "  ")
                .goe("i.quantity", "minQuantity", null)
                .in("o.id", "ids", List.of());

        assertThat(condition.toWhereXml()).isEmpty();
        assertThat(condition.fragmentsForTest()).isEmpty();
    }

    @Test
    void buildsEqLikeRangeInFragments() {
        var condition = ConditionBuilder.create()
                .eq("o.status", "status", OrderStatusStub.P)
                .like("o.customer_name", "customerName", "Kim")
                .range("o.created_at", "createdFrom", "createdTo", "a", "b", false)
                .in("o.id", "ids", List.of(1L, 2L))
                .goe("i.quantity", "minQuantity", 5);

        String xml = condition.toWhereXml();
        assertThat(xml).startsWith("<where>");
        assertThat(xml).contains("o.status = #{status}");
        assertThat(xml).contains("LOWER(o.customer_name) LIKE");
        assertThat(xml).contains("o.created_at &gt;=");
        assertThat(xml).contains("o.created_at &lt; #{createdTo}");
        assertThat(xml).contains("foreach");
        assertThat(xml).contains("i.quantity &gt;= #{minQuantity}");
    }

    private enum OrderStatusStub { P }
}
