package com.example.enumapp.domain.order;

import com.example.enumapp.common.code.AbstractCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter extends AbstractCodeEnumConverter<OrderStatus> {
    public OrderStatusConverter() {
        super(OrderStatus.class);
    }
}
