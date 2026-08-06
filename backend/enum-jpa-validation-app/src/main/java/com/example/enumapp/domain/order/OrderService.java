package com.example.enumapp.domain.order;

import com.example.enumapp.common.time.DateStrings;
import com.example.enumapp.web.ApiMessageCodes;
import com.example.enumapp.web.BusinessException;
import com.example.enumapp.web.dto.OrderDateSearchRequest;
import com.example.enumapp.web.dto.OrderFlexibleSearchRequest;
import com.example.enumapp.web.dto.OrderRequest;
import com.example.enumapp.web.dto.OrderResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = toEntity(request);
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        applyItems(order, request.getItems());
        orderRepository.save(order);
        return get(order.getId());
    }

    @Transactional
    public OrderResponse createWithCreatedAt(OrderRequest request, LocalDateTime createdAt) {
        Order order = toEntity(request);
        order.setCreatedAt(createdAt);
        applyItems(order, request.getItems());
        orderRepository.save(order);
        return get(order.getId());
    }

    @Transactional
    public OrderResponse update(OrderRequest request) {
        Order existing = orderRepository.findWithItemsById(request.getId())
                .orElseThrow(() -> BusinessException.notFound(
                        ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + request.getId()
                ));

        existing.setCustomerName(request.getCustomerName());
        existing.setUserGrade(request.getUserGrade());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getPayMethod() != null) {
            existing.setPayMethod(request.getPayMethod());
        }
        applyItems(existing, request.getItems());
        return get(existing.getId());
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id
                ));
        return toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(
                        ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id
                ));
        if (existing.getStatus() == OrderStatus.CANCELLED) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "order already cancelled: " + id);
        }
        existing.setStatus(OrderStatus.CANCELLED);
        return get(id);
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
        return mapList(orderRepository.findAllByOrderByIdAsc());
    }

    /**
     * 유연 검색: 날짜/between/숫자/enum/문자열 — 모두 비어 있으면 전체 조회.
     * Criteria Builder → JPA Specification.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> searchFlexible(OrderFlexibleSearchRequest request) {
        OrderFlexibleSearchCriteria criteria = OrderFlexibleSearchCriteriaBuilder.from(request);
        return mapList(orderRepository.findAll(
                OrderFlexibleSearchSpecifications.from(criteria),
                Sort.by(Sort.Direction.ASC, "id")
        ));
    }

    @Transactional
    public List<OrderResponse> updateStatusMany(List<Long> ids, OrderStatus status) {
        if (status == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "status is required for bulk update");
        }
        for (Long id : ids) {
            Order existing = orderRepository.findById(id)
                    .orElseThrow(() -> BusinessException.notFound(
                            ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id
                    ));
            existing.setStatus(status);
        }
        return mapList(orderRepository.findByIdInOrderByIdAsc(ids));
    }

    @Transactional
    public void deleteMany(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        orderRepository.deleteAllById(ids);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchBySingleDate(OrderDateSearchRequest request) {
        OrderSearchCriteria criteria = toCriteria(request);
        if (criteria.getOrderDate() == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "orderDate is required");
        }
        LocalDateTime from = criteria.getOrderDate().atStartOfDay();
        LocalDateTime to = criteria.getOrderDate().plusDays(1).atStartOfDay();
        return mapList(orderRepository.searchByCreatedAtRange(from, to, criteria.getMinQuantity()));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchByDateBetween(OrderDateSearchRequest request) {
        OrderSearchCriteria criteria = toCriteria(request);
        if (criteria.getFromDate() == null || criteria.getToDate() == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "fromDate and toDate are required");
        }
        LocalDateTime from = criteria.getFromDate().atStartOfDay();
        LocalDateTime to = criteria.getToDate().plusDays(1).atStartOfDay();
        return mapList(orderRepository.searchByCreatedAtRange(from, to, criteria.getMinQuantity()));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchByDateTimeBetween(OrderDateSearchRequest request) {
        OrderSearchCriteria criteria = toCriteria(request);
        if (criteria.getFromDateTime() == null || criteria.getToDateTime() == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "fromDateTime and toDateTime are required");
        }
        return mapList(orderRepository.searchByCreatedAtDateTimeBetween(
                criteria.getFromDateTime(),
                criteria.getToDateTime(),
                criteria.getMinQuantity()
        ));
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
        return orders.stream().map(this::toResponse).toList();
    }

    private void applyItems(Order order, List<OrderRequest.OrderItemRequest> items) {
        List<OrderItem> mapped = items.stream().map(itemRequest -> {
            OrderItem item = new OrderItem();
            item.setProductName(itemRequest.getProductName());
            item.setSku(itemRequest.getSku());
            item.setQuantity(itemRequest.getQuantity());
            return item;
        }).toList();
        order.replaceItems(mapped);
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
