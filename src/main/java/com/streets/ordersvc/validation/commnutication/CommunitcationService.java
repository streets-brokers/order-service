package com.streets.ordersvc.validation.commnutication;

import org.springframework.stereotype.Component;

@Component
// TODO: this is used to communicate with the payment and portfolio service
// for the necessary information
public interface CommunitcationService {

    // Does a remote call to the payment service to get the balance for a client
    Double getBalance(Long clientID);


    // Does a remote call to the portfolio service to get the quantity of a
    // particular stock using its ticker symbol
    Double getStockQuantityByTicker(Long clientId, String ticker);
}
