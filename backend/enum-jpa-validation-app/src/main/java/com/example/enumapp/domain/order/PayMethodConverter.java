package com.example.enumapp.domain.order;

import com.example.enumapp.common.code.AbstractCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PayMethodConverter extends AbstractCodeEnumConverter<PayMethod> {
    public PayMethodConverter() {
        super(PayMethod.class);
    }
}
