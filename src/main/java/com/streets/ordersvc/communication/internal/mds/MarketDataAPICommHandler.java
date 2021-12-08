package com.streets.ordersvc.communication.internal.mds;

import com.streets.ordersvc.communication.responses.ExchangeDataPayload;
import com.streets.ordersvc.communication.responses.FullOrderBook;
import com.streets.ordersvc.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;



public class MarketDataAPICommHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataAPICommHandler.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    public static ExchangeDataPayload[] getMarketDataByProduct(String product) {
        String uri = PropertiesReader.getProperty("MARKET_DATA_SERVICE_URL") + "/market/products/" + product;
        LOGGER.info("Going to get the market data for product: ");
        try {
            return restTemplate.getForObject(uri, ExchangeDataPayload[].class);
        } catch (RestClientException e) {
            LOGGER.info("Could not get the product data for: " + product);
            throw new RestClientException(e.getMessage());
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            throw new RestClientException(e.getMessage());
        }
    }

    public static String[] getMarketProducts() {
        String uri = PropertiesReader.getProperty("MARKET_DATA_SERVICE_URL") + "/market/products/tickers";
        LOGGER.info("loading product tickers: ");
        try {
            return restTemplate.getForObject(uri, String[].class);
        } catch (RestClientException e) {
            LOGGER.info("Could not load the products:");
            throw new RestClientException(e.getMessage());
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            throw new RestClientException(e.getMessage());
        }
    }


}
