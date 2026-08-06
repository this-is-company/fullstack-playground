package com.example.enumapp.common.code;

import com.example.enumapp.domain.order.Order;
import com.example.enumapp.domain.order.OrderItem;
import com.example.enumapp.domain.order.OrderMapper;
import com.example.enumapp.domain.order.OrderStatus;
import com.example.enumapp.domain.order.PayMethod;
import com.example.enumapp.domain.order.UserGrade;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CodeEnumMyBatisIntegrationTest {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void registersTypeHandlers_forAllCodeEnums() {
        assertThat(CodeEnumMyBatisConfig.isRegistered(sqlSessionFactory, OrderStatus.class)).isTrue();
        assertThat(CodeEnumMyBatisConfig.isRegistered(sqlSessionFactory, PayMethod.class)).isTrue();
        assertThat(CodeEnumMyBatisConfig.isRegistered(sqlSessionFactory, UserGrade.class)).isTrue();
    }

    @Test
    void persistsAndLoads_enumsByCode_andAllowsNullEnums() {
        Order order = new Order();
        order.setCustomerName("null-enum-customer");
        order.setStatus(null);
        order.setPayMethod(null);
        order.setUserGrade(UserGrade.BASIC);
        orderMapper.insertOrder(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductName("Pen");
        item.setSku("SKU-1");
        item.setQuantity(1);
        orderMapper.insertItem(item);

        Order loaded = orderMapper.findById(order.getId());
        assertThat(loaded.getStatus()).isNull();
        assertThat(loaded.getPayMethod()).isNull();
        assertThat(loaded.getUserGrade()).isEqualTo(UserGrade.BASIC);

        loaded.setStatus(OrderStatus.PAID);
        loaded.setPayMethod(PayMethod.CARD);
        orderMapper.updateOrder(loaded);

        Order updated = orderMapper.findById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updated.getPayMethod()).isEqualTo(PayMethod.CARD);
        assertThat(updated.getStatus().getCode()).isEqualTo("A");
    }
}
