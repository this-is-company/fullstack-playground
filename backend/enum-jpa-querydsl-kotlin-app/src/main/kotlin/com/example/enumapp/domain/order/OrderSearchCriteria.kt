package com.example.enumapp.domain.order

import java.time.LocalDate
import java.time.LocalDateTime

class OrderSearchCriteria {
    var orderDate: LocalDate? = null
    /** exclusive end (orderDate + 1 day) */
    var orderDateEnd: LocalDate? = null
    var fromDate: LocalDate? = null
    var toDate: LocalDate? = null
    /** exclusive end (toDate + 1 day) */
    var toDateEnd: LocalDate? = null
    var fromDateTime: LocalDateTime? = null
    var toDateTime: LocalDateTime? = null
    var minQuantity: Int? = null
}
