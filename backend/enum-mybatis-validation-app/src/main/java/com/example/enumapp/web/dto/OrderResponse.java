package com.example.enumapp.web.dto;

import com.example.enumapp.domain.order.OrderStatus;
import com.example.enumapp.domain.order.PayMethod;
import com.example.enumapp.domain.order.UserGrade;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 응답. enum 필드는 code, *Description 은 한글 설명.")
public class OrderResponse {

    private Long id;
    private String customerName;

    @Schema(description = "주문 상태 code", example = "P")
    private OrderStatus status;

    @Schema(description = "주문 상태 설명 (예: 대기)")
    private String statusDescription;

    @Schema(description = "결제수단 code", example = "CARD")
    private PayMethod payMethod;

    @Schema(description = "결제수단 설명")
    private String payMethodDescription;

    @Schema(description = "회원 등급 code", example = "G")
    private UserGrade userGrade;

    @Schema(description = "회원 등급 설명")
    private String userGradeDescription;

    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        this.statusDescription = status == null ? null : status.getDescription();
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public PayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(PayMethod payMethod) {
        this.payMethod = payMethod;
        this.payMethodDescription = payMethod == null ? null : payMethod.getDescription();
    }

    public String getPayMethodDescription() {
        return payMethodDescription;
    }

    public UserGrade getUserGrade() {
        return userGrade;
    }

    public void setUserGrade(UserGrade userGrade) {
        this.userGrade = userGrade;
        this.userGradeDescription = userGrade == null ? null : userGrade.getDescription();
    }

    public String getUserGradeDescription() {
        return userGradeDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public static class OrderItemResponse {
        private Long id;
        private String productName;
        private String sku;
        private Integer quantity;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
