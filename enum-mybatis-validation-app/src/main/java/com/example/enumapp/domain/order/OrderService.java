package com.example.enumapp.domain.order;

import com.example.enumapp.common.time.DateStrings;
import com.example.enumapp.web.ApiMessageCodes;
import com.example.enumapp.web.BusinessException;
import com.example.enumapp.web.dto.OrderDateSearchRequest;
import com.example.enumapp.web.dto.OrderRequest;
import com.example.enumapp.web.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = toEntity(request);
        orderMapper.insertOrder(order);
        saveItems(order.getId(), request.getItems());
        return get(order.getId());
    }

    @Transactional
    public OrderResponse createWithCreatedAt(OrderRequest request, LocalDateTime createdAt) {
        Order order = toEntity(request);
        order.setCreatedAt(createdAt);
        orderMapper.insertOrder(order);
        saveItems(order.getId(), request.getItems());
        return get(order.getId());
    }

    @Transactional
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
        return get(order.getId());
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        List<OrderItem> items = orderMapper.findItemsByOrderId(id);
        order.setItems(items);
        return toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        Order existing = orderMapper.findById(id);
        if (existing == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        orderMapper.deleteItemsByOrderId(id);
        orderMapper.deleteOrder(id);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        Order existing = orderMapper.findById(id);
        if (existing == null) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: " + id);
        }
        if (existing.getStatus() == OrderStatus.CANCELLED) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "order already cancelled: " + id);
        }
        orderMapper.updateStatus(id, OrderStatus.CANCELLED);
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
        return mapList(orderMapper.findAll());
    }

    @Transactional
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
        return mapList(orderMapper.findByIds(ids));
    }

    @Transactional
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
     * 문자열 날짜 → LocalDate/LocalDateTime 변환.
     * minQuantity 는 Integer 그대로 유지 (null 이면 null, 0 으로 바꾸지 않음).
     */
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
