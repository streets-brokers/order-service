package com.streets.ordersvc.common.dao.repositories;

import com.streets.ordersvc.common.dao.models.Order;
import com.streets.ordersvc.common.enums.OrderStatus;
import com.streets.ordersvc.common.enums.Side;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    @Query(value = "SELECT * FROM orders WHERE client_id = ?1", nativeQuery = true)
    List<Order> findByClientId(Long clientId);

    @Query(value = "SELECT * FROM orders WHERE status = 2", nativeQuery = true)
    List<Order> getExecutingOrders();

    @Query(value = "SELECT * FROM orders WHERE status = ?1 AND side = ?2", nativeQuery = true)
    List<Order> getOrdersByStatusAndSide(OrderStatus status, String side);

    @Query(value = "SELECT * FROM orders WHERE status =?1", nativeQuery = true)
    List<Order> getOrdersByStatus(OrderStatus status);

    @Query(value = "SELECT * FROM orders WHERE side =?1", nativeQuery = true)
    List<Order> getOrdersBySide(String side);
}