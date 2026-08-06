package com.example.enumapp.web.dto;

import com.example.enumapp.domain.order.OrderStatus;
import com.example.enumapp.domain.order.PayMethod;
import com.example.enumapp.web.validation.LocalDateString;
import com.example.enumapp.web.validation.LocalDateTimeString;
import com.example.enumapp.web.validation.NoSpecialChars;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 모든 필드가 비어 있어도 된다. 값이 있는 조건만 AND 로 적용한다.
 */
@Schema(description = "유연 검색. 날짜/between/숫자/enum/문자열 — 전부 optional.")
public class OrderFlexibleSearchRequest {

    @Schema(description = "고객명 부분 일치", nullable = true, example = "Kim")
    @NoSpecialChars
    private String customerName;

    @Schema(description = "주문 상태 code", nullable = true, example = "P")
    private OrderStatus status;

    @Schema(description = "결제수단 code", nullable = true, example = "CARD")
    private PayMethod payMethod;

    @Schema(description = "최소 수량. null 이면 미적용", nullable = true, example = "2")
    private Integer minQuantity;

    @Schema(description = "단일 일자. 있으면 해당일 00:00~다음날 00:00", example = "2026-08-06")
    @LocalDateString
    private String orderDate;

    @Schema(description = "between 시작일", example = "2026-08-01")
    @LocalDateString
    private String fromDate;

    @Schema(description = "between 종료일(포함, 다음날 00:00 미만)", example = "2026-08-03")
    @LocalDateString
    private String toDate;

    @Schema(description = "between 시작일시", example = "2026-08-06T09:00:00")
    @LocalDateTimeString
    private String fromDateTime;

    @Schema(description = "between 종료일시(포함)", example = "2026-08-06T18:00:00")
    @LocalDateTimeString
    private String toDateTime;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(PayMethod payMethod) {
        this.payMethod = payMethod;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(String fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public String getToDateTime() {
        return toDateTime;
    }

    public void setToDateTime(String toDateTime) {
        this.toDateTime = toDateTime;
    }
}
