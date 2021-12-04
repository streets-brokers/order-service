package com.streets.ordersvc.processing;

import com.streets.ordersvc.common.ExecutableOrder;
import com.streets.ordersvc.dao.models.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StrategySelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(StrategySelector.class);

    public static ExecutableOrder assignStrategy(Order order) {
        // What information is needed to decide on what strategy to use
        // Prices from all exchanges and quantity available
        // The price shifts to decide to hold or execute immediately
        return null;
    }
}
