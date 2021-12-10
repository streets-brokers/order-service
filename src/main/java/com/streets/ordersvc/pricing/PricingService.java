package com.streets.ordersvc.pricing;

import com.streets.ordersvc.common.dao.models.Order;
import org.springframework.stereotype.Component;

@Component
public interface PricingService {
    Boolean isReasonable(Order order);
    Double computeBestPrice(Order order);
}
