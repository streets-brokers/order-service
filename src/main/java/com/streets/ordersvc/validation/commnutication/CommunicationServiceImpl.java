package com.streets.ordersvc.validation.commnutication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CommunicationServiceImpl implements CommunitcationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationServiceImpl.class);

    @Override
    public Double getBalance(String clientID) {
        return null;
    }

    @Override
    public Double getStockQuantityByTicker(String clientId, String ticker) {
        return null;
    }
}
