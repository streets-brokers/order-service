package com.streets.ordersvc.dao.repositories;

import com.streets.ordersvc.dao.models.Order;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    @Query("select * from order u where u.clientId = ?1")
    List<Order> findByClientId(Long clientId);
}