package com.example.enumapp.web.dto;

import com.example.enumapp.web.validation.LocalDateString;
import com.example.enumapp.web.validation.LocalDateTimeString;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 날짜는 문자열로 받고, 서비스에서 LocalDate / LocalDateTime 으로 변환한다.
 * minQuantity 는 Wrapper(Integer) 라서 JSON null 이 0 으로 바뀌지 않아야 한다.
 */
@Schema(description = "주문 날짜/수량 검색 조건. 사용하는 조회 API에 맞는 필드만 채운다.")
public class OrderDateSearchRequest {

    @Schema(description = "단일 일자 조회. yyyy-MM-dd → LocalDate", example = "2026-08-06")
    @LocalDateString
    private String orderDate;

    @Schema(description = "between 시작일. yyyy-MM-dd → LocalDate", example = "2026-08-01")
    @LocalDateString
    private String fromDate;

    @Schema(description = "between 종료일. yyyy-MM-dd → LocalDate", example = "2026-08-03")
    @LocalDateString
    private String toDate;

    @Schema(description = "between 시작일시. yyyy-MM-dd'T'HH:mm:ss → LocalDateTime", example = "2026-08-06T09:00:00")
    @LocalDateTimeString
    private String fromDateTime;

    @Schema(description = "between 종료일시. yyyy-MM-dd'T'HH:mm:ss → LocalDateTime", example = "2026-08-06T18:00:00")
    @LocalDateTimeString
    private String toDateTime;

    @Schema(description = "최소 수량 필터. null 이면 필터 미적용(0 으로 바뀌지 않음)", nullable = true, example = "5")
    private Integer minQuantity;

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

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }
}
