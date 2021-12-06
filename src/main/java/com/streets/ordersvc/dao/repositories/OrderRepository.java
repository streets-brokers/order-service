package com.streets.ordersvc.dao.repositories;

import com.streets.ordersvc.dao.models.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    @Query(value = "SELECT * FROM orders WHERE client_id = ?1", nativeQuery = true)
    List<Order> findByClientId(Long clientId);
}