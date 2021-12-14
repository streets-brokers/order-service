package com.streets.ordersvc.communication.internal.mds;

import com.streets.ordersvc.communication.responses.ExchangeDataPayload;
import com.streets.ordersvc.communication.responses.TrendResult;
import com.streets.ordersvc.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
public class MarketDataAPICommHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataAPICommHandler.class);
    @Autowired
    private RestTemplate restTemplate;


    public  ExchangeDataPayload[] getMarketDataByProduct(String product) {
        String uri = PropertiesReader.getProperty("MARKET_DATA_SERVICE_URL") + "/products/" + product;
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
    public TrendResult[] getMarketTrendByProduct(String product) {
        String uri = PropertiesReader.getProperty("MARKET_DATA_SERVICE_URL") + "/trends/" + product;
        LOGGER.info("Going to get the market data for product: ");
        try {
            return restTemplate.getForObject(uri, TrendResult[].class);
        } catch (RestClientException e) {
            LOGGER.info("Could not get the product data for: " + product);
            throw new RestClientException(e.getMessage());
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            throw new RestClientException(e.getMessage());
        }
    }

    public  String[] getMarketProducts() {
        String uri = PropertiesReader.getProperty("MARKET_DATA_SERVICE_URL") + "/products/tickers";
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
