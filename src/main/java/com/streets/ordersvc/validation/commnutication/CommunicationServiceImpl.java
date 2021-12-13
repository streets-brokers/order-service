package com.streets.ordersvc.validation.commnutication;

import com.streets.ordersvc.communication.internal.payment.AccountAPICommHandler;
import com.streets.ordersvc.communication.responses.AccountResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommunicationServiceImpl implements CommunitcationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationServiceImpl.class);
    private final AccountAPICommHandler handler;

    @Autowired
    public CommunicationServiceImpl(AccountAPICommHandler handler) {
        this.handler = handler;
    }

    @Override
    public Double getBalance(Long clientID) {
        try {
            AccountResponse response = this.handler.getAccountById(clientID);
            if (response == null || response.getTotalBalance() == null) {
                return 0.0;
            }
            return response.getTotalBalance();
        } catch (Exception e) {
            return 0.0;
        }

    }

    @Override
    public Double getStockQuantityByTicker(Long clientId, String ticker) {
        return null;
    }
}
