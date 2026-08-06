package com.example.enumapp.domain.order

import java.time.LocalDateTime

/**
 * 유연 검색용 typed criteria. null 필드는 조건에 넣지 않는다.
 */
class OrderFlexibleSearchCriteria private constructor(
    val customerName: String?,
    val status: OrderStatus?,
    val payMethod: PayMethod?,
    val minQuantity: Int?,
    val createdFrom: LocalDateTime?,
    val createdTo: LocalDateTime?,
    val createdToInclusive: Boolean
) {

    fun isEmpty(): Boolean =
        customerName == null &&
            status == null &&
            payMethod == null &&
            minQuantity == null &&
            createdFrom == null &&
            createdTo == null

    class Builder {
        private var customerName: String? = null
        private var status: OrderStatus? = null
        private var payMethod: PayMethod? = null
        private var minQuantity: Int? = null
        private var createdFrom: LocalDateTime? = null
        private var createdTo: LocalDateTime? = null
        private var createdToInclusive: Boolean = false

        fun customerName(customerName: String?): Builder = apply {
            this.customerName = customerName
        }

        fun status(status: OrderStatus?): Builder = apply {
            this.status = status
        }

        fun payMethod(payMethod: PayMethod?): Builder = apply {
            this.payMethod = payMethod
        }

        fun minQuantity(minQuantity: Int?): Builder = apply {
            this.minQuantity = minQuantity
        }

        fun createdFrom(createdFrom: LocalDateTime?): Builder = apply {
            this.createdFrom = createdFrom
        }

        fun createdTo(createdTo: LocalDateTime?, inclusive: Boolean): Builder = apply {
            this.createdTo = createdTo
            this.createdToInclusive = inclusive
        }

        fun build(): OrderFlexibleSearchCriteria =
            OrderFlexibleSearchCriteria(
                customerName = customerName,
                status = status,
                payMethod = payMethod,
                minQuantity = minQuantity,
                createdFrom = createdFrom,
                createdTo = createdTo,
                createdToInclusive = createdToInclusive
            )
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
