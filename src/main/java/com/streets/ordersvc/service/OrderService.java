package com.streets.ordersvc.service;

import com.streets.ordersvc.common.enums.OrderStatus;
import com.streets.ordersvc.common.enums.Side;
import com.streets.ordersvc.common.types.Tuple2;
import com.streets.ordersvc.communication.internal.mds.MarketDataAPICommHandler;
import com.streets.ordersvc.communication.responses.ExchangeDataPayload;
import com.streets.ordersvc.dao.models.Leg;
import com.streets.ordersvc.dao.models.Order;
import com.streets.ordersvc.dao.repositories.OrderRepository;
import com.streets.ordersvc.processing.scan.PriceQuantityScanningService;
import com.streets.ordersvc.processing.scan.ScanResult;
import com.streets.ordersvc.validation.services.ValidationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository repository;

    private final String[] xs = {"EXCHANGE1", "EXCHANGE2"};


    private final ValidationServiceImpl validationService;


    private final PriceQuantityScanningService priceScanner;

    @Autowired
    public OrderService(OrderRepository repository, ValidationServiceImpl validationService, PriceQuantityScanningService priceScanner) {
        this.repository = repository;
        this.validationService = validationService;
        this.priceScanner = priceScanner;
    }

    public Order placeOrder(Order d) {
        // TODO: make a request to the market data service to get the current market price
        List<ExchangeDataPayload> prices;
        try {
            prices = Arrays.asList(MarketDataAPICommHandler.getMarketDataByProduct(d.getProduct()));
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "prices could not be found for the product: " + d.getProduct() + " due to " + e.getMessage());
        }

        if (prices.size() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "prices could not be found for the product: " + d.getProduct());
        }


        // validate amount
        Tuple2<Boolean, String> amountValidationResult = validationService.isValidAmount(d);
        if (!amountValidationResult.getIsValid()) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, amountValidationResult.getMsg());
        }

        // validate quantity
        Tuple2<Boolean, String> quantityValidationResult = validationService.isValidQuantity(d);
        if (!quantityValidationResult.getIsValid()) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, quantityValidationResult.getMsg());
        }

        // validate rate
        Tuple2<Boolean, String> rateValidationResult = validationService.isValidRate(d);
        if (!rateValidationResult.getIsValid()) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, rateValidationResult.getMsg());
        }

        if (Objects.equals(d.getSide(), Side.BUY.toString())) {
            prices.sort(Comparator.comparingDouble(ExchangeDataPayload::getBidPrice));
            d.setMarketPrice(prices.get(0).getBidPrice());
        } else if (Objects.equals(d.getSide(), Side.SELL.toString())) {
            prices.sort(Comparator.comparingDouble(ExchangeDataPayload::getAskPrice).reversed());
            d.setMarketPrice(prices.get(0).getAskPrice());
        }
        d.setStatus(OrderStatus.PENDING);

        // save the order to get and ID
        this.repository.save(d);

        Side side = Side.valueOf(d.getSide());
        Integer totalQuantity = d.getQuantity();

        // go scan the order book and return the result
        List<ScanResult> results = priceScanner.scanBook(xs, d.getProduct(), side);
        if (side == Side.BUY) {
            // sort in ascending order
            results.sort(Comparator.comparingDouble(ScanResult::getMinPrice));
        } else {
            // sort in descending order
            results.sort(Comparator.comparingDouble(ScanResult::getMaxPrice).reversed());

        }
        Set<Leg> orderLegs = new HashSet<>();
        // split till quantity is fulfilled
        for (ScanResult result : results) {
            if (totalQuantity > 0) {
                Leg leg = new Leg();
                leg.setProduct(d.getProduct());
                leg.setSide(d.getSide());
                leg.setXchange(result.getExchange());
                leg.setOrder(d);
                leg.setQuantity(Math.min(result.getQuantity(), totalQuantity));
                totalQuantity -= leg.getQuantity();
                // pick the minimum between the market price and the lowest price from the open orders
                if (Objects.equals(leg.getSide(), Side.BUY.toString())) {
                    leg.setPrice(Math.min(result.getMinPrice(), d.getMarketPrice()));
                } else {
                    // pick the max between the market price and the highest price from the open orders
                    leg.setPrice(Math.max(result.getMaxPrice(), d.getMarketPrice()));
                }
                orderLegs.add(leg);
            }
        }
        d.setLegs(orderLegs);
        this.repository.save(d);
        return d;
    }

    public Order getOrderById(Long id) {
        Optional<Order> d = this.repository.findById(id);
        if (d.isPresent()) {
            return d.get();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "no order exists with id: " + id);
        }
    }

    public List<Order> listUserOrders(Long id) {
        return  this.repository.findByClientId(id);
    }
}
