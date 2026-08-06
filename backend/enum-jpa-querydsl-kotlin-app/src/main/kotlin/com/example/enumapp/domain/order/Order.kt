package com.example.enumapp.domain.order

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "kqdsl_demo_orders")
open class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @Column(name = "customer_name", nullable = false, length = 100)
    open var customerName: String? = null

    @Column(name = "status", length = 20)
    open var status: OrderStatus? = null

    @Column(name = "pay_method", length = 20)
    open var payMethod: PayMethod? = null

    @Column(name = "user_grade", nullable = false, length = 20)
    open var userGrade: UserGrade? = null

    @Column(name = "created_at", nullable = false)
    open var createdAt: LocalDateTime? = null

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var items: MutableList<OrderItem> = mutableListOf()

    fun replaceItems(newItems: List<OrderItem>) {
        items.clear()
        for (item in newItems) {
            addItem(item)
        }
    }

    fun addItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }
}
