package com.example.enumapp.domain.order;

import com.example.enumapp.common.time.DateStrings;
import com.example.enumapp.web.dto.OrderFlexibleSearchRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Request(전부 optional) → typed {@link OrderFlexibleSearchCriteria}.
 * 빈 문자열/null 은 조건에서 제외한다.
 */
public final class OrderFlexibleSearchCriteriaBuilder {

    private OrderFlexibleSearchCriteriaBuilder() {
    }

    public static OrderFlexibleSearchCriteria from(OrderFlexibleSearchRequest request) {
        OrderFlexibleSearchCriteria.Builder builder = OrderFlexibleSearchCriteria.builder();
        if (request == null) {
            return builder.build();
        }

        if (hasText(request.getCustomerName())) {
            builder.customerName(request.getCustomerName().trim());
        }
        if (request.getStatus() != null) {
            builder.status(request.getStatus());
        }
        if (request.getPayMethod() != null) {
            builder.payMethod(request.getPayMethod());
        }
        if (request.getMinQuantity() != null) {
            builder.minQuantity(request.getMinQuantity());
        }

        applyCreatedAtRange(builder, request);
        return builder.build();
    }

    /**
     * 우선순위: orderDate &gt; fromDate/toDate &gt; fromDateTime/toDateTime
     * (동시에 여러 날짜 그룹이 와도 하나만 적용)
     */
    private static void applyCreatedAtRange(
            OrderFlexibleSearchCriteria.Builder builder,
            OrderFlexibleSearchRequest request
    ) {
        if (hasText(request.getOrderDate())) {
            LocalDate day = DateStrings.toLocalDate(request.getOrderDate());
            builder.createdFrom(day.atStartOfDay());
            builder.createdTo(day.plusDays(1).atStartOfDay(), false);
            return;
        }

        boolean hasDateBound = hasText(request.getFromDate()) || hasText(request.getToDate());
        if (hasDateBound) {
            if (hasText(request.getFromDate())) {
                builder.createdFrom(DateStrings.toLocalDate(request.getFromDate()).atStartOfDay());
            }
            if (hasText(request.getToDate())) {
                builder.createdTo(
                        DateStrings.toLocalDate(request.getToDate()).plusDays(1).atStartOfDay(),
                        false
                );
            }
            return;
        }

        if (hasText(request.getFromDateTime()) || hasText(request.getToDateTime())) {
            if (hasText(request.getFromDateTime())) {
                builder.createdFrom(DateStrings.toLocalDateTime(request.getFromDateTime()));
            }
            if (hasText(request.getToDateTime())) {
                LocalDateTime to = DateStrings.toLocalDateTime(request.getToDateTime());
                builder.createdTo(to, true);
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
