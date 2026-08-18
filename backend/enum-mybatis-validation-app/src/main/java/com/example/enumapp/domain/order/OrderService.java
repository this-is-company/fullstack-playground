package com.example.enumapp.domain.order;

import com.example.enumapp.common.time.DateStrings;
import com.example.enumapp.web.ApiMessageCodes;
import com.example.enumapp.web.BusinessException;
import com.example.enumapp.web.dto.OrderDateSearchRequest;
import com.example.enumapp.web.dto.OrderFlexibleSearchRequest;
import com.example.enumapp.web.dto.OrderRequest;
import com.example.enumapp.web.dto.OrderResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderQueryService orderQueryService;

    public OrderService(OrderMapper orderMapper, OrderQueryService orderQueryService) {
        this.orderMapper = orderMapper;
        this.orderQueryService = orderQueryService;
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "orders", key = "#result.id"),
            evict = {
                    @CacheEvict(value = "orders-list", allEntries = true),
                    @CacheEvict(value = "orders-search", allEntries = true)
            }
    )
    public OrderResponse create(OrderRequest request) {
        Order order = toEntity(request);
        orderMapper.insertOrder(order);
        saveItems(order.getId(), request.getItems());
        return orderQueryService.fetchFromDb(order.getId());
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "orders", key = "#result.id"),
            evict = {
                    @CacheEvict(value = "orders-list", allEntries = true),
                    @CacheEvict(value = "orders-search", allEntries = true)
            }
    )
    public OrderResponse createWithCreatedAt(OrderRequest request, LocalDateTime createdAt) {
        Order order = toEntity(request);
        order.setCreatedAt(createdAt);
        orderMapper.insertOrder(order);
        saveItems(order.getId(), request.getItems());
        return orderQueryService.fetchFromDb(order.getId());
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "orders", key = "#result.id"),
            evict = {
                    @CacheEvict(value = "orders-list", allEntries = true),
                    @CacheEvict(value = "orders-search", allEntries = true)
            }
    )
    public OrderResponse update(OrderRequest request) {
        Order existing = orderMapper.findById(request.getId());
        if (existing == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + request.getId());
        }
        Order order = toEntity(request);
        if (order.getStatus() == null) {
            order.setStatus(existing.getStatus());
        }
        if (order.getPayMethod() == null) {
            order.setPayMethod(existing.getPayMethod());
        }
        orderMapper.updateOrder(order);
        orderMapper.deleteItemsByOrderId(order.getId());
        saveItems(order.getId(), request.getItems());
        return orderQueryService.fetchFromDb(order.getId());
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return orderQueryService.getById(id);
    }

    /**
     * {@code @CachePut} 예제: 메서드는 항상 실행되고, 반환값으로 {@code orders} 캐시를 덮어쓴다.
     * 다음 GET 은 DB 없이 이 값을 쓴다.
     */
    @Transactional
    @CachePut(value = "orders", key = "#id")
    public OrderResponse cachePutRename(Long id, String customerName) {
        Order existing = orderMapper.findById(id);
        if (existing == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        existing.setCustomerName(customerName);
        orderMapper.updateOrder(existing);
        return orderQueryService.fetchFromDb(id);
    }

    /**
     * {@code @CacheEvict} 예제: {@code orders} 키만 지운다. DB 행은 그대로 둔다.
     * 다음 GET 은 캐시 miss → 다시 DB.
     */
    @CacheEvict(value = "orders", key = "#id")
    public void cacheEvictById(Long id) {
        // 캐시만 삭제. 조회는 하지 않아도 된다.
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orders", key = "#id"),
            @CacheEvict(value = "orders-list", allEntries = true),
            @CacheEvict(value = "orders-search", allEntries = true)
    })
    public void delete(Long id) {
        Order existing = orderMapper.findById(id);
        if (existing == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        orderMapper.deleteItemsByOrderId(id);
        orderMapper.deleteOrder(id);
    }

    @Transactional
    @Caching(
            put = @CachePut(value = "orders", key = "#result.id"),
            evict = {
                    @CacheEvict(value = "orders-list", allEntries = true),
                    @CacheEvict(value = "orders-search", allEntries = true)
            }
    )
    public OrderResponse cancel(Long id) {
        Order existing = orderMapper.findById(id);
        if (existing == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        if (existing.getStatus() == OrderStatus.CANCELLED) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "order already cancelled: " + id);
        }
        orderMapper.updateStatus(id, OrderStatus.CANCELLED);
        return orderQueryService.fetchFromDb(id);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(OrderDateSearchRequest request) {
        if (request.getOrderDate() != null && !request.getOrderDate().isBlank()) {
            return searchBySingleDate(request);
        }
        if (request.getFromDate() != null && !request.getFromDate().isBlank()
                && request.getToDate() != null && !request.getToDate().isBlank()) {
            return searchByDateBetween(request);
        }
        if (request.getFromDateTime() != null && !request.getFromDateTime().isBlank()
                && request.getToDateTime() != null && !request.getToDateTime().isBlank()) {
            return searchByDateTimeBetween(request);
        }
        return orderQueryService.findAll();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orders-list", allEntries = true),
            @CacheEvict(value = "orders-search", allEntries = true)
    })
    public List<OrderResponse> updateStatusMany(List<Long> ids, OrderStatus status) {
        if (status == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "status is required for bulk update");
        }
        for (Long id : ids) {
            Order existing = orderMapper.findById(id);
            if (existing == null) {
                throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
            }
            orderMapper.updateStatus(id, status);
        }
        return orderQueryService.fetchByIdsFromDb(ids);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orders", allEntries = true),
            @CacheEvict(value = "orders-list", allEntries = true),
            @CacheEvict(value = "orders-search", allEntries = true)
    })
    public void deleteMany(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        orderMapper.deleteItemsByOrderIds(ids);
        orderMapper.deleteOrders(ids);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchBySingleDate(OrderDateSearchRequest request) {
        OrderSearchCriteria criteria = toCriteria(request);
        if (criteria.getOrderDate() == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "orderDate is required");
        }
        criteria.setOrderDateEnd(criteria.getOrderDate().plusDays(1));
        return mapList(orderMapper.searchBySingleDate(criteria));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchByDateBetween(OrderDateSearchRequest request) {
        OrderSearchCriteria criteria = toCriteria(request);
        if (criteria.getFromDate() == null || criteria.getToDate() == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "fromDate and toDate are required");
        }
        criteria.setToDateEnd(criteria.getToDate().plusDays(1));
        return mapList(orderMapper.searchByDateBetween(criteria));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchByDateTimeBetween(OrderDateSearchRequest request) {
        OrderSearchCriteria criteria = toCriteria(request);
        if (criteria.getFromDateTime() == null || criteria.getToDateTime() == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "fromDateTime and toDateTime are required");
        }
        return mapList(orderMapper.searchByDateTimeBetween(criteria));
    }

    /**
     * 유연 검색: Criteria Builder → MyBatis 동적 SQL (&lt;if&gt;).
     * 모든 조건이 비어 있으면 전체 목록(캐시 orders-list).
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> searchFlexible(OrderFlexibleSearchRequest request) {
        OrderFlexibleSearchCriteria criteria = OrderFlexibleSearchCriteriaBuilder.from(request);
        if (criteria.isEmpty()) {
            return orderQueryService.findAll();
        }
        return orderQueryService.searchFlexible(criteria);
    }

    public OrderSearchCriteria toCriteria(OrderDateSearchRequest request) {
        OrderSearchCriteria criteria = new OrderSearchCriteria();
        criteria.setOrderDate(DateStrings.toLocalDate(request.getOrderDate()));
        criteria.setFromDate(DateStrings.toLocalDate(request.getFromDate()));
        criteria.setToDate(DateStrings.toLocalDate(request.getToDate()));
        criteria.setFromDateTime(DateStrings.toLocalDateTime(request.getFromDateTime()));
        criteria.setToDateTime(DateStrings.toLocalDateTime(request.getToDateTime()));
        criteria.setMinQuantity(request.getMinQuantity());
        return criteria;
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

    private void saveItems(Long orderId, List<OrderRequest.OrderItemRequest> items) {
        for (OrderRequest.OrderItemRequest itemRequest : items) {
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductName(itemRequest.getProductName());
            item.setSku(itemRequest.getSku());
            item.setQuantity(itemRequest.getQuantity());
            orderMapper.insertItem(item);
        }
    }

    private Order toEntity(OrderRequest request) {
        Order order = new Order();
        order.setId(request.getId());
        order.setCustomerName(request.getCustomerName());
        order.setStatus(request.getStatus());
        order.setPayMethod(request.getPayMethod());
        order.setUserGrade(request.getUserGrade());
        return order;
    }
}
