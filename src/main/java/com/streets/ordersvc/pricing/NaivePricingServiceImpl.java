package com.streets.ordersvc.pricing;

import com.streets.ordersvc.common.dao.models.Order;

public class NaivePricingServiceImpl implements PricingService {
    @Override
    public Boolean isReasonable(Order order) {
        return null;
    }

    @Override
    public Double computeBestPrice(Order order) {
        return null;
    }
}
