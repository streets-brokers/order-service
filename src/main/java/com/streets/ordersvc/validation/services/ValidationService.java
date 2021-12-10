package com.streets.ordersvc.validation.services;

import com.streets.ordersvc.common.types.Tuple2;
import com.streets.ordersvc.common.dao.models.Order;
import org.springframework.stereotype.Component;

@Component
// TODO: (romeo) probably break down this validation into those related to the client
// and those related to the order itself
public interface ValidationService {
    // Check that a client has sufficient funds

    /**
     * Hit the payment service to check if the client has enough funds
     * Returns a tuple with the first value being a boolean
     * and the second value being a message that could be shown to the use
     *
     * @param order
     */
    Tuple2<Boolean, String> isValidAmount(Order order);


    // Check that a client owns the stock they are selling (unless it's a short position)

    /**
     * Hit the portfolio service to check if the client has the stock, if it's not a short position
     * Returns a tuple with the first value being a boolean
     * and the second value being a message that could be shown to the use
     *
     * @param order
     */
    // TODO: Does the quantity really need to be a double? Maybe we'll support buying
    // fractional shares but definitely not at the moment
    Tuple2<Boolean, String> isValidQuantity(Order order);


    // Limit checking - Rate limit the orders the client is sending

    /**
     * Hold an accumulator to track client's orders for a trading day
     * Another accumulator to track the number of orders per second
     * Another accumulator to track the value of the order
     * PS: All these accumulators could simply merge into a single object
     *
     * @param order
     */
    Tuple2<Boolean, String> isValidRate(Order order);


    // Does the order have chance of being accepted

    /**
     * How do we score an order and check if it could succeed or not
     *
     * @param order
     */
    Tuple2<Boolean, String> hasValidScore(Order order);
}
