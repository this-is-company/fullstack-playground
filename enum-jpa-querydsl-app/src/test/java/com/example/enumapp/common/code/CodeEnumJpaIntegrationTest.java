package com.example.enumapp.common.code;

import com.example.enumapp.domain.order.Order;
import com.example.enumapp.domain.order.OrderItem;
import com.example.enumapp.domain.order.OrderRepository;
import com.example.enumapp.domain.order.OrderStatus;
import com.example.enumapp.domain.order.PayMethod;
import com.example.enumapp.domain.order.UserGrade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CodeEnumJpaIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void persistsAndLoads_enumsByCode_andAllowsNullEnums() {
        Order order = new Order();
        order.setCustomerName("null-enum-customer");
        order.setStatus(null);
        order.setPayMethod(null);
        order.setUserGrade(UserGrade.BASIC);
        order.setCreatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setProductName("Pen");
        item.setSku("SKU-1");
        item.setQuantity(1);
        order.addItem(item);

        orderRepository.save(order);

        Order loaded = orderRepository.findWithItemsById(order.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isNull();
        assertThat(loaded.getPayMethod()).isNull();
        assertThat(loaded.getUserGrade()).isEqualTo(UserGrade.BASIC);

        loaded.setStatus(OrderStatus.PAID);
        loaded.setPayMethod(PayMethod.CARD);
        orderRepository.save(loaded);

        Order updated = orderRepository.findWithItemsById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updated.getPayMethod()).isEqualTo(PayMethod.CARD);
        assertThat(updated.getStatus().getCode()).isEqualTo("A");
    }
}
