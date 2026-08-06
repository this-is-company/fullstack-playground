package com.example.enumapp.domain.order;

import java.time.LocalDateTime;

/**
 * 유연 검색용 typed criteria. null 필드는 조건에 넣지 않는다.
 */
public class OrderFlexibleSearchCriteria {

    private final String customerName;
    private final OrderStatus status;
    private final PayMethod payMethod;
    private final Integer minQuantity;
    private final LocalDateTime createdFrom;
    private final LocalDateTime createdTo;
    private final boolean createdToInclusive;

    private OrderFlexibleSearchCriteria(Builder builder) {
        this.customerName = builder.customerName;
        this.status = builder.status;
        this.payMethod = builder.payMethod;
        this.minQuantity = builder.minQuantity;
        this.createdFrom = builder.createdFrom;
        this.createdTo = builder.createdTo;
        this.createdToInclusive = builder.createdToInclusive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCustomerName() {
        return customerName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PayMethod getPayMethod() {
        return payMethod;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public LocalDateTime getCreatedFrom() {
        return createdFrom;
    }

    public LocalDateTime getCreatedTo() {
        return createdTo;
    }

    public boolean isCreatedToInclusive() {
        return createdToInclusive;
    }

    public boolean isEmpty() {
        return customerName == null
                && status == null
                && payMethod == null
                && minQuantity == null
                && createdFrom == null
                && createdTo == null;
    }

    public static final class Builder {
        private String customerName;
        private OrderStatus status;
        private PayMethod payMethod;
        private Integer minQuantity;
        private LocalDateTime createdFrom;
        private LocalDateTime createdTo;
        private boolean createdToInclusive;

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder payMethod(PayMethod payMethod) {
            this.payMethod = payMethod;
            return this;
        }

        public Builder minQuantity(Integer minQuantity) {
            this.minQuantity = minQuantity;
            return this;
        }

        public Builder createdFrom(LocalDateTime createdFrom) {
            this.createdFrom = createdFrom;
            return this;
        }

        public Builder createdTo(LocalDateTime createdTo, boolean inclusive) {
            this.createdTo = createdTo;
            this.createdToInclusive = inclusive;
            return this;
        }

        public OrderFlexibleSearchCriteria build() {
            return new OrderFlexibleSearchCriteria(this);
        }
    }
}
