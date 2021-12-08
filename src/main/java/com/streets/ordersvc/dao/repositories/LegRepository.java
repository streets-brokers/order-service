package com.streets.ordersvc.dao.repositories;

import com.streets.ordersvc.dao.models.Leg;
import com.streets.ordersvc.dao.models.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LegRepository extends CrudRepository<Leg, Long> {
    @Query(value = "SELECT * FROM legs WHERE order_id = ?1", nativeQuery = true)
    List<Leg> findByOrderId(Long orderId);
}

