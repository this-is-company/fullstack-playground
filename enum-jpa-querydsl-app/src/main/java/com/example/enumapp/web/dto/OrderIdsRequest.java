package com.example.enumapp.web.dto;

import com.example.enumapp.domain.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "다건 수정/삭제 요청")
public class OrderIdsRequest {

    @NotEmpty
    @Schema(description = "대상 주문 id 목록", example = "[1, 2, 3]")
    private List<Long> ids;

    @Schema(description = "다건 수정(POST /update) 시 변경할 상태 코드. 삭제(POST /delete) 시에는 무시", example = "A", nullable = true)
    private OrderStatus status;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
