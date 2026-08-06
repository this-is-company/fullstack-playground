package com.example.enumapp.domain.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    int insertOrder(Order order);

    int insertItem(OrderItem item);

    Order findById(@Param("id") Long id);

    List<OrderItem> findItemsByOrderId(@Param("orderId") Long orderId);

    int updateOrder(Order order);

    int updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);

    int deleteItemsByOrderId(@Param("orderId") Long orderId);

    int deleteOrder(@Param("id") Long id);

    int deleteOrders(@Param("ids") List<Long> ids);

    int deleteItemsByOrderIds(@Param("orderIds") List<Long> orderIds);

    List<Order> findAll();

    List<Order> findByIds(@Param("ids") List<Long> ids);

    List<Order> searchBySingleDate(OrderSearchCriteria criteria);

    List<Order> searchByDateBetween(OrderSearchCriteria criteria);

    List<Order> searchByDateTimeBetween(OrderSearchCriteria criteria);
}
