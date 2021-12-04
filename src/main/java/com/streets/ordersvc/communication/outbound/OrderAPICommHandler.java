package com.streets.ordersvc.communication.outbound;

import com.streets.ordersvc.communication.requests.OrderRequestBody;
import com.streets.ordersvc.communication.responses.FullOrderBook;
import com.streets.ordersvc.communication.responses.OrderBookItem;
import com.streets.ordersvc.common.enums.Side;
import com.streets.ordersvc.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderAPICommHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAPICommHandler.class);
    private static final RestTemplate restTemplate = new RestTemplate();

    // TODO:(romeo) though nobody gives a shit about this primary key
    // you may want to make is more secure
    private static final String apiKey = PropertiesReader.getProperty("API_KEY");

    public static String placeOrder(String baseURL, OrderRequestBody body) {
        String uri = baseURL + "/" + apiKey + "/order";


        HttpEntity<OrderRequestBody> entity = new HttpEntity<>(body, getHeaders());

        LOGGER.info("New order request sent to: " + uri);
        LOGGER.info("New order request: " + body.toString());
        try {
            return restTemplate.postForObject(uri, entity, String.class);
        } catch (RestClientException e) {
            LOGGER.info("Order creation failed: " + body);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    //     TODO: response needs to be a boolean so fix this shit
    public static void updateOrder(String baseURL, OrderRequestBody body, String id) {
        String uri = baseURL + "/" + apiKey + "/order/" + id;
        HttpEntity<OrderRequestBody> entity = new HttpEntity<>(body, getHeaders());

        LOGGER.info("Update specific order with id:" + id + "at:" + uri);
        LOGGER.info("New order update request: " + body.toString());
        try {
            restTemplate.put(uri, entity);
        } catch (RestClientException e) {
            LOGGER.info("Order creation failed: " + body);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }

    }

    public static OrderBookItem getOrderItemById(String baseURL, String id) {
        String uri = baseURL + "/" + apiKey + "/order/" + id;
        LOGGER.info("Getting specific order from: " + uri + "with id: " + id);
        List<FullOrderBook> books = null;
        try {
            return restTemplate.getForObject(uri, OrderBookItem.class);
        } catch (RestClientException e) {
            LOGGER.info("Could not read order for id: " + id + "from:" + uri);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    // TODO: response needs to be a boolean so fix this shit
    public static void cancelOrder(String baseURL, String id) {
        String uri = baseURL + "/" + apiKey + "/order/" + id;


        LOGGER.info("Cancelling an order from: " + uri + "with id:" + id);
        try {
            restTemplate.delete(uri, Boolean.class);
        } catch (RestClientException e) {
            LOGGER.info("Failed to cancel order with id:" + id);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
    }


    /*
     * These are wrappers for communicating with the order book api without having to pass all the parameters
     * */
    public static FullOrderBook getClosedOrderBookByProduct(String baseURL, String product) {
        return getOrderBookByProductAndKey(baseURL, product, "closed");
    }

    public static FullOrderBook getBuyOpenedOrderBookByProduct(String baseURL, String product) {
        FullOrderBook orderBook = getOpenedOrderBookByProduct(baseURL, product);
        if (orderBook != null)
            orderBook.setFullOrderBook(orderBook.getFullOrderBook().stream().filter(o -> Objects.equals(o.getSide(), Side.BUY.toString())).collect(Collectors.toList()));
        return orderBook;
    }
    public static FullOrderBook getSellOpenedOrderBookByProduct(String baseURL, String product) {
        FullOrderBook orderBook = getOpenedOrderBookByProduct(baseURL, product);
        if (orderBook != null)
            orderBook.setFullOrderBook(orderBook.getFullOrderBook().stream().filter(o -> Objects.equals(o.getSide(), Side.SELL.toString())).collect(Collectors.toList()));
        return orderBook;
    }

    public static FullOrderBook getOpenedOrderBookByProduct(String baseURL, String product) {
        return getOrderBookByProductAndKey(baseURL, product, "open");
    }

    public static FullOrderBook getSellOrderBookByProduct(String baseURL, String product) {
        return getOrderBookByProductAndKey(baseURL, product, Side.SELL.toString());
    }

    public static FullOrderBook getBuyOrderBookByProduct(String baseURL, String product) {
        return getOrderBookByProductAndKey(baseURL, product, Side.BUY.toString());
    }

    public static FullOrderBook getCancelledOrderBookByProduct(String baseURL, String product) {
        return getOrderBookByProductAndKey(baseURL, product, "cancelled");
    }

    public static List<FullOrderBook> getOrderBook(String baseURL) {
        String uri = baseURL + "/orderbook";
        LOGGER.info("Reading the entire order book for: " + uri);
        List<FullOrderBook> books = null;
        try {
            FullOrderBook[] fullOrderBooks = restTemplate.getForObject(uri, FullOrderBook[].class);
            if (fullOrderBooks != null)
                return List.of(fullOrderBooks);
            return null;
        } catch (RestClientException e) {
            LOGGER.info("Could not read the order book for: " + uri);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    public static FullOrderBook getOrderBookByProduct(String baseURL, String product) {
        String uri = baseURL + "/orderbook/" + product;
        LOGGER.info("Reading the order book for: " + product + " from " + uri);
        try {
            return restTemplate.getForObject(uri, FullOrderBook.class);
        } catch (RestClientException e) {
            LOGGER.info("Could not read the order book for: " + uri);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    public static FullOrderBook getOrderBookByProductAndKey(String baseURL, String product, String key) {
        String uri = baseURL + "/orderbook/" + product + "/" + key;
        LOGGER.info("Reading the order book for: " + product + " from " + uri + "with  key: " + key);
        try {
            return restTemplate.getForObject(uri, FullOrderBook.class);
        } catch (RestClientException e) {
            LOGGER.info("Could not read the order book for: " + uri);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    private static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // set `content-type` header
        headers.setContentType(MediaType.APPLICATION_JSON);
        // set `accept` header
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }


}
