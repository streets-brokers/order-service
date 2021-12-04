package com.streets.ordersvc.common;

import com.streets.ordersvc.dao.models.Order;
import com.streets.ordersvc.processing.strategy.Strategy;

public class ExecutableOrder {
    private Order order;
    private Strategy strategy;
}
