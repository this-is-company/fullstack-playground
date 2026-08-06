package com.example.enumapp.domain.order;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderSearchCriteria {

    private LocalDate orderDate;
    /** exclusive end (orderDate + 1 day) */
    private LocalDate orderDateEnd;
    private LocalDate fromDate;
    private LocalDate toDate;
    /** exclusive end (toDate + 1 day) */
    private LocalDate toDateEnd;
    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
    private Integer minQuantity;

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getOrderDateEnd() {
        return orderDateEnd;
    }

    public void setOrderDateEnd(LocalDate orderDateEnd) {
        this.orderDateEnd = orderDateEnd;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public LocalDate getToDateEnd() {
        return toDateEnd;
    }

    public void setToDateEnd(LocalDate toDateEnd) {
        this.toDateEnd = toDateEnd;
    }

    public LocalDateTime getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(LocalDateTime fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public LocalDateTime getToDateTime() {
        return toDateTime;
    }

    public void setToDateTime(LocalDateTime toDateTime) {
        this.toDateTime = toDateTime;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }
}
