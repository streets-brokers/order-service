package com.streets.ordersvc.communication.internal.payment;

import com.streets.ordersvc.communication.responses.AccountResponse;
import com.streets.ordersvc.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class AccountAPICommHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAPICommHandler.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    public AccountResponse getAccountById(Long clientId) {
        String uri = PropertiesReader.getProperty("ACCOUNT_SERVICE_URL") + "/account/clientId/" + clientId;
        LOGGER.info("Going to get the market data for product: ");
        try {
            return restTemplate.getForObject(uri, AccountResponse.class);
        } catch (RestClientException e) {
            LOGGER.info("Could retrieve account details for the client with ID" + clientId);
            throw new RestClientException(Objects.requireNonNull(e.getMessage()));
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            throw new RestClientException(e.getMessage());
        }
    }
}
