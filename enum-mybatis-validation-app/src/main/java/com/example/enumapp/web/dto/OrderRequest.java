package com.example.enumapp.web.dto;

import com.example.enumapp.domain.order.OrderStatus;
import com.example.enumapp.domain.order.PayMethod;
import com.example.enumapp.domain.order.UserGrade;
import com.example.enumapp.web.validation.NotBlankFieldsInList;
import com.example.enumapp.web.validation.NoSpecialChars;
import com.example.enumapp.web.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "주문 생성/수정 요청. enum 은 code 문자열로 주고받는다 (예: status=P).")
public class OrderRequest {

    @Schema(description = "생성 시 null. 수정 시 path 의 id 를 쓰므로 body 에 넣지 않아도 됨", nullable = true)
    @Null(groups = ValidationGroups.Create.class, message = "id must be null on create")
    private Long id;

    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @NoSpecialChars(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String customerName;

    @Schema(description = "주문 상태 code. 생성 시 필수, 수정 시 null/blank 이면 기존 값 유지", example = "P", nullable = true)
    @NotNull(groups = ValidationGroups.Create.class, message = "status is required on create")
    private OrderStatus status;

    @Schema(description = "결제수단 code. 생성 시 필수, 수정 시 null/blank 이면 기존 값 유지", example = "CARD", nullable = true)
    @NotNull(groups = ValidationGroups.Create.class, message = "payMethod is required on create")
    private PayMethod payMethod;

    @Schema(description = "회원 등급 code", example = "G")
    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private UserGrade userGrade;

    /**
     * @Pattern 단순 패턴 검증 예시 필드.
     * null 이면 검사하지 않음. 값이 있으면 영문/숫자/_/- 만 허용.
     */
    @Schema(description = "@Pattern 예시. 영문/숫자/_/- 만 허용", example = "PROMO_01", nullable = true)
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "promoCode must match pattern [A-Za-z0-9_-]+",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}
    )
    private String promoCode;

    @Schema(description = "주문 상품 목록. null/empty 및 내부 blank 필드 불가")
    @NotBlankFieldsInList(
            fields = {"productName", "sku"},
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}
    )
    @Valid
    private List<OrderItemRequest> items;

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
    }

    public PayMethod getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(PayMethod payMethod) {
        this.payMethod = payMethod;
    }

    public UserGrade getUserGrade() {
        return userGrade;
    }

    public void setUserGrade(UserGrade userGrade) {
        this.userGrade = userGrade;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public static class OrderItemRequest {
        @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        @NoSpecialChars(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        private String productName;

        @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        @NoSpecialChars(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        private String sku;

        @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        @Positive(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
        private Integer quantity;

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
