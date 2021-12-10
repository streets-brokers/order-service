package com.streets.ordersvc.validation.services;

import com.streets.ordersvc.common.types.Tuple2;
import com.streets.ordersvc.common.dao.models.Order;
import com.streets.ordersvc.validation.commnutication.CommunicationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationServiceImpl.class);

    private final CommunicationServiceImpl communicationService;

    @Autowired
    public ValidationServiceImpl(CommunicationServiceImpl communicationService) {
        this.communicationService = communicationService;
    }

    /**
     * Hit the payment service to check if the client has enough funds
     * Returns a tuple with the first value being a boolean
     * and the second value being a message that could be shown to the use
     *
     * @param order
     */
    @Override
    public Tuple2<Boolean, String> isValidAmount(Order order) {
        Double totalBalance = communicationService.getBalance(order.getClientId());
        if (totalBalance < order.getValue()) {
            return new Tuple2<>(false, "Not enough funds to fulfill order");
        }

        return new Tuple2<>(true, "");
    }

    @Override
    public Tuple2<Boolean, String> isValidQuantity(Order order) {
        return new Tuple2<>(true, "");
    }

    /**
     * Hold an accumulator to track client's orders for a trading day
     * Another accumulator to track the number of orders per second
     * Another accumulator to track the value of the order
     * PS: All these accumulators could simply merge into a single object
     *
     * @param order
     */
    @Override
    public Tuple2<Boolean, String> isValidRate(Order order) {
        return new Tuple2<>(true, "");
    }

    @Override
    public Tuple2<Boolean, String> hasValidScore(Order order) {

        return new Tuple2<>(true, "");
    }
}
