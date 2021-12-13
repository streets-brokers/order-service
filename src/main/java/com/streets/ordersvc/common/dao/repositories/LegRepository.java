package com.streets.ordersvc.common.dao.repositories;

import com.streets.ordersvc.common.dao.models.Leg;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LegRepository extends CrudRepository<Leg, Long> {
    @Query(value = "SELECT * FROM legs WHERE order_id = ?1", nativeQuery = true)
    List<Leg> findByOrderId(Long orderId);
}

