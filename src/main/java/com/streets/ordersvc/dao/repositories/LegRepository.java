package com.streets.ordersvc.dao.repositories;

import com.streets.ordersvc.dao.models.Leg;
import org.springframework.data.repository.CrudRepository;

public interface LegRepository extends CrudRepository<Leg, Long> {
}
