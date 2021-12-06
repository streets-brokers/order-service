package com.streets.ordersvc.service;

import com.streets.ordersvc.common.enums.OrderStatus;
import com.streets.ordersvc.common.enums.Side;
import com.streets.ordersvc.common.types.Tuple2;
import com.streets.ordersvc.communication.internal.mds.MarketDataAPICommHandler;
import com.streets.ordersvc.communication.outbound.OrderAPICommHandler;
import com.streets.ordersvc.communication.requests.OrderRequestBody;
import com.streets.ordersvc.communication.responses.ExchangeDataPayload;
import com.streets.ordersvc.dao.models.Leg;
import com.streets.ordersvc.dao.models.Order;
import com.streets.ordersvc.dao.repositories.LegRepository;
import com.streets.ordersvc.dao.repositories.OrderRepository;
import com.streets.ordersvc.processing.scan.PriceQuantityScanner;
import com.streets.ordersvc.processing.scan.ScanResult;
import com.streets.ordersvc.utils.PropertiesReader;
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
    private final OrderRepository orderRepository;
    private final LegRepository legRepository;

    private final String[] xs = {"EXCHANGE1", "EXCHANGE2"};


    private final ValidationServiceImpl validationService;


    private final PriceQuantityScanner priceScanner;

    @Autowired
    public OrderService(OrderRepository repository, LegRepository legRepository, ValidationServiceImpl validationService, PriceQuantityScanner priceScanner) {
        this.orderRepository = repository;
        this.legRepository = legRepository;
        this.validationService = validationService;
        this.priceScanner = priceScanner;
    }

    public Order placeOrder(Order clientOrder) {
        // TODO: make a request to the market data service to get the current market price
        List<ExchangeDataPayload> prices;
        try {
            prices = Arrays.asList(MarketDataAPICommHandler.getMarketDataByProduct(clientOrder.getProduct()));
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "prices could not be found for the product: " + clientOrder.getProduct() + " due to " + e.getMessage());
        }

        if (prices.size() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "prices could not be found for the product: " + clientOrder.getProduct());
        }


        // validate amount
        Tuple2<Boolean, String> amountValidationResult = validationService.isValidAmount(clientOrder);
        if (!amountValidationResult.getIsValid()) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, amountValidationResult.getMsg());
        }

        // validate quantity
        Tuple2<Boolean, String> quantityValidationResult = validationService.isValidQuantity(clientOrder);
        if (!quantityValidationResult.getIsValid()) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, quantityValidationResult.getMsg());
        }

        // validate rate
        Tuple2<Boolean, String> rateValidationResult = validationService.isValidRate(clientOrder);
        if (!rateValidationResult.getIsValid()) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, rateValidationResult.getMsg());
        }

        if (clientOrder.getSide().equals(Side.BUY.toString())) {
            prices.sort(Comparator.comparingDouble(ExchangeDataPayload::getBidPrice));
            clientOrder.setMarketPrice(prices.get(0).getBidPrice());
        } else if (clientOrder.getSide().equals(Side.SELL.toString())) {
            prices.sort(Comparator.comparingDouble(ExchangeDataPayload::getAskPrice).reversed());
            clientOrder.setMarketPrice(prices.get(0).getAskPrice());
        }
        clientOrder.setStatus(OrderStatus.PENDING);

        // save the order to get and ID
        this.orderRepository.save(clientOrder);

        Side side = Side.valueOf(clientOrder.getSide());
        Integer totalQuantity = clientOrder.getQuantity();

        // go scan the order book and return the result
        List<ScanResult> results = priceScanner.scanBook(xs, clientOrder.getProduct(), side);
        if (side == Side.BUY) {
            // sort in ascending order
            results.sort(Comparator.comparingDouble(ScanResult::getMinPrice));
        } else {
            // sort in descending order
            results.sort(Comparator.comparingDouble(ScanResult::getMaxPrice).reversed());

        }
        results.forEach(System.out::println);
        Set<Leg> orderLegs = new HashSet<>();
        // split till quantity is fulfilled
        for (ScanResult result : results) {
            if (totalQuantity > 0) {
                Leg leg = new Leg();
                leg.setProduct(clientOrder.getProduct());
                leg.setSide(clientOrder.getSide());
                leg.setXchange(result.getExchange());
                leg.setOrder(clientOrder);
                leg.setQuantity(Math.min(result.getQuantity(), totalQuantity));
                totalQuantity -= leg.getQuantity();
                leg.setMarketPrice(clientOrder.getMarketPrice());
                // pick the minimum between the market price and the lowest price from the open orders
                if (leg.getSide().equals(Side.BUY.toString())) {
                    leg.setPrice(Math.min(clientOrder.getPrice(), clientOrder.getMarketPrice()));
                    LOGGER.info("Buying at: " + leg.getPrice() + " at a market price of: " + clientOrder.getMarketPrice());
                } else {
                    // pick the max between the market price and the highest price from the open orders
                    leg.setPrice(Math.max(clientOrder.getPrice(), clientOrder.getMarketPrice()));
                    LOGGER.info("Selling at: " + leg.getPrice() + " at a market price of: " + clientOrder.getMarketPrice());
                }
                orderLegs.add(leg);
            }
        }
        // Now go ahead and place the orders
        // TODO: parallelize this shit
        for (Leg leg : orderLegs) {
            OrderRequestBody body = new OrderRequestBody(leg.getProduct(), leg.getQuantity(), leg.getPrice(), leg.getSide());
            String xid = OrderAPICommHandler.placeOrder(PropertiesReader.getProperty(leg.getXchange() + "_BASE_URL"), body);
            if (xid != null) {
                leg.setXid(xid.replaceAll("^\"|\"$", ""));
                leg.setStatus(OrderStatus.EXECUTING);
                this.legRepository.save(leg);
            }


        }
        clientOrder.setLegs(orderLegs);
        boolean isExecuting = false;
        for (Leg leg : clientOrder.getLegs()) {
            if (leg.getStatus() == OrderStatus.EXECUTING) {
                isExecuting = true;
                break;
            }
        }
        if (isExecuting) {
            clientOrder.setStatus(OrderStatus.EXECUTING);
        }
        this.orderRepository.save(clientOrder);
        Optional<Order> savedOrder = this.orderRepository.findById(clientOrder.getId());
        return savedOrder.orElse(null);
    }

    public Order getOrderById(Long id) {
        Optional<Order> d = this.orderRepository.findById(id);
        if (d.isPresent()) {
            return d.get();
        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "no order exists with id: " + id);
        }
    }

    public List<Order> listUserOrders(Long id) {
        return this.orderRepository.findByClientId(id);
    }
}
