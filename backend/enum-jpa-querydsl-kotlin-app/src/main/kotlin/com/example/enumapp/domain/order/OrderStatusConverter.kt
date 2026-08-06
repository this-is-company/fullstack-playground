package com.example.enumapp.domain.order

import com.example.enumapp.common.code.AbstractCodeEnumConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class OrderStatusConverter : AbstractCodeEnumConverter<OrderStatus>(OrderStatus::class.java)
