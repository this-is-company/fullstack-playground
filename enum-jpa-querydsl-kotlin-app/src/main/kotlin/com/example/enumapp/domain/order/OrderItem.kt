package com.example.enumapp.domain.order

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "kqdsl_demo_order_items")
open class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    open var order: Order? = null

    @Column(name = "product_name", nullable = false, length = 100)
    open var productName: String? = null

    @Column(name = "sku", nullable = false, length = 50)
    open var sku: String? = null

    @Column(name = "quantity", nullable = false)
    open var quantity: Int? = null
}
