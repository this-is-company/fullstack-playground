package com.example.enumapp.common.code

import com.example.enumapp.domain.order.Order
import com.example.enumapp.domain.order.OrderItem
import com.example.enumapp.domain.order.OrderRepository
import com.example.enumapp.domain.order.OrderStatus
import com.example.enumapp.domain.order.PayMethod
import com.example.enumapp.domain.order.UserGrade
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class CodeEnumJpaIntegrationTest {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Test
    fun persistsAndLoads_enumsByCode_andAllowsNullEnums() {
        val order = Order().apply {
            customerName = "null-enum-customer"
            status = null
            payMethod = null
            userGrade = UserGrade.BASIC
            createdAt = LocalDateTime.now()
        }

        val item = OrderItem().apply {
            productName = "Pen"
            sku = "SKU-1"
            quantity = 1
        }
        order.addItem(item)

        orderRepository.save(order)

        val loaded = orderRepository.findWithItemsById(order.id!!).orElseThrow()
        assertThat(loaded.status).isNull()
        assertThat(loaded.payMethod).isNull()
        assertThat(loaded.userGrade).isEqualTo(UserGrade.BASIC)

        loaded.status = OrderStatus.PAID
        loaded.payMethod = PayMethod.CARD
        orderRepository.save(loaded)

        val updated = orderRepository.findWithItemsById(order.id!!).orElseThrow()
        assertThat(updated.status).isEqualTo(OrderStatus.PAID)
        assertThat(updated.payMethod).isEqualTo(PayMethod.CARD)
        assertThat(updated.status!!.code).isEqualTo("A")
    }
}
