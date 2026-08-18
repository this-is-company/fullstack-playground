package com.example.enumapp.domain.order;

import com.example.enumapp.web.ApiMessageCodes;
import com.example.enumapp.web.BusinessException;
import com.example.enumapp.web.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 조회 전용 — {@link Cacheable} 이 여기서 동작한다.
 * Controller → OrderService → (여기) → OrderMapper → DB
 */
@Service
public class OrderQueryService {

    private static final Logger log = LoggerFactory.getLogger(OrderQueryService.class);

    private final OrderMapper orderMapper;

    public OrderQueryService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Cacheable(value = "orders", key = "#id")
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        log.info("[CACHE→DB] getById id={}", id);
        return fetchFromDb(id);
    }

    @Cacheable(value = "orders-list", key = "'all'")
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        log.info("[CACHE→DB] findAll orders");
        return mapList(orderMapper.findAll());
    }

    @Cacheable(value = "orders-search", key = "#criteria.cacheKey()")
    @Transactional(readOnly = true)
    public List<OrderResponse> searchFlexible(OrderFlexibleSearchCriteria criteria) {
        log.info("[CACHE→DB] searchFlexible key={}", criteria.cacheKey());
        return mapList(orderMapper.searchFlexible(criteria));
    }

    /** 쓰기 후 캐시 갱신용 — 캐시 어노테이션 없음, 항상 DB 조회 */
    @Transactional(readOnly = true)
    public OrderResponse fetchFromDb(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        order.setItems(orderMapper.findItemsByOrderId(id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> fetchByIdsFromDb(List<Long> ids) {
        return mapList(orderMapper.findByIds(ids));
    }

    private List<OrderResponse> mapList(List<Order> orders) {
        return orders.stream().map(order -> {
            order.setItems(orderMapper.findItemsByOrderId(order.getId()));
            return toResponse(order);
        }).toList();
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setStatus(order.getStatus());
        response.setPayMethod(order.getPayMethod());
        response.setUserGrade(order.getUserGrade());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(order.getItems().stream().map(item -> {
            OrderResponse.OrderItemResponse r = new OrderResponse.OrderItemResponse();
            r.setId(item.getId());
            r.setProductName(item.getProductName());
            r.setSku(item.getSku());
            r.setQuantity(item.getQuantity());
            return r;
        }).toList());
        return response;
    }
}
