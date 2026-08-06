package com.example.enumapp.domain.order;

import com.example.enumapp.web.dto.OrderFlexibleSearchRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderFlexibleSearchCriteriaBuilderTest {

    @Test
    void emptyRequest_buildsEmptyCriteria() {
        OrderFlexibleSearchCriteria criteria = OrderFlexibleSearchCriteriaBuilder.from(new OrderFlexibleSearchRequest());
        assertThat(criteria.isEmpty()).isTrue();
    }

    @Test
    void blankStringsAndNulls_areIgnored() {
        OrderFlexibleSearchRequest request = new OrderFlexibleSearchRequest();
        request.setCustomerName("  ");
        request.setOrderDate("");
        request.setFromDate(null);
        request.setMinQuantity(null);
        assertThat(OrderFlexibleSearchCriteriaBuilder.from(request).isEmpty()).isTrue();
    }

    @Test
    void orderDate_winsOverBetweenFields() {
        OrderFlexibleSearchRequest request = new OrderFlexibleSearchRequest();
        request.setOrderDate("2026-08-06");
        request.setFromDate("2026-01-01");
        request.setToDate("2026-12-31");

        OrderFlexibleSearchCriteria criteria = OrderFlexibleSearchCriteriaBuilder.from(request);
        assertThat(criteria.getCreatedFrom()).isEqualTo(LocalDateTime.of(2026, 8, 6, 0, 0));
        assertThat(criteria.getCreatedTo()).isEqualTo(LocalDateTime.of(2026, 8, 7, 0, 0));
        assertThat(criteria.isCreatedToInclusive()).isFalse();
    }

    @Test
    void mapsEnumNumberAndString() {
        OrderFlexibleSearchRequest request = new OrderFlexibleSearchRequest();
        request.setCustomerName(" Kim ");
        request.setStatus(OrderStatus.PENDING);
        request.setPayMethod(PayMethod.CARD);
        request.setMinQuantity(3);

        OrderFlexibleSearchCriteria criteria = OrderFlexibleSearchCriteriaBuilder.from(request);
        assertThat(criteria.getCustomerName()).isEqualTo("Kim");
        assertThat(criteria.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(criteria.getPayMethod()).isEqualTo(PayMethod.CARD);
        assertThat(criteria.getMinQuantity()).isEqualTo(3);
    }
}
