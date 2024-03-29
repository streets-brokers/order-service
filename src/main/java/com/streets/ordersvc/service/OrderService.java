package com.streets.ordersvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streets.ordersvc.common.dao.models.Leg;
import com.streets.ordersvc.common.dao.models.Order;
import com.streets.ordersvc.common.dao.repositories.LegRepository;
import com.streets.ordersvc.common.dao.repositories.OrderRepository;
import com.streets.ordersvc.common.enums.OrderStatus;
import com.streets.ordersvc.common.enums.Side;
import com.streets.ordersvc.common.types.Tuple2;
import com.streets.ordersvc.communication.internal.mds.MarketDataAPICommHandler;
import com.streets.ordersvc.communication.outbound.OrderAPICommHandler;
import com.streets.ordersvc.communication.requests.OrderRequestBody;
import com.streets.ordersvc.communication.responses.ExchangeDataPayload;
import com.streets.ordersvc.lr.Payload;
import com.streets.ordersvc.processing.strategy.analyzers.PQAnalyzer;
import com.streets.ordersvc.processing.strategy.analyzers.TrendAnalyzer;
import com.streets.ordersvc.processing.strategy.results.PQAnalysisResult;
import com.streets.ordersvc.queue.RedisMessagePublisher;
import com.streets.ordersvc.utils.PropertiesReader;
import com.streets.ordersvc.validation.services.ValidationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LegRepository legRepository;
    @Autowired
    private OrderAPICommHandler orderAPICommHandler;
    @Autowired
    private RedisMessagePublisher messagePublisher;
    @Autowired
    private MarketDataAPICommHandler marketDataAPICommHandler;

    private final String[] xs = {"EXCHANGE1", "EXCHANGE2"};


    @Autowired
    private ValidationServiceImpl validationService;


    @Autowired
    private PQAnalyzer pqAnalyzer;
    @Autowired
    private TrendAnalyzer trendAnalyzer;


    public Order placeOrder(Order clientOrder) {
        clientOrder.setValue(clientOrder.getPrice() * clientOrder.getQuantity());
        // TODO: make a request to the market data service to get the current market price
        List<ExchangeDataPayload> prices;
        try {
            prices = Arrays.asList(marketDataAPICommHandler.getMarketDataByProduct(clientOrder.getProduct()));
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "prices could not be found for the product: " + clientOrder.getProduct() + " due to " + e.getMessage());
        }

        if (prices.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "prices could not be found for the product: " + clientOrder.getProduct());
        }


        if (clientOrder.getSide().equals(Side.BUY.toString())) {
            // validate amount
            Tuple2<Boolean, String> amountValidationResult = validationService.isValidAmount(clientOrder);
            if (!amountValidationResult.getIsValid()) {
                throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, amountValidationResult.getMsg());
            }
        }

        if (clientOrder.getSide().equals(Side.SELL.toString())) {
            // validate quantity
            Tuple2<Boolean, String> quantityValidationResult = validationService.isValidQuantity(clientOrder);
            if (!quantityValidationResult.getIsValid()) {
                throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, quantityValidationResult.getMsg());
            }
        }


        // validate rate
        Tuple2<Boolean, String> rateValidationResult = validationService.isValidRate(clientOrder);
        if (!rateValidationResult.getIsValid()) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, rateValidationResult.getMsg());
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

        Side side = Side.valueOf(clientOrder.getSide().toUpperCase());
        Integer totalQuantity = clientOrder.getQuantity();

        // go scan the order book and return the result
        List<PQAnalysisResult> results = pqAnalyzer.analyze(xs, clientOrder.getProduct(), side);
        if (side == Side.BUY) {
            // sort in ascending order
            results.sort(Comparator.comparingDouble(PQAnalysisResult::getMinPrice));
        } else {
            // sort in descending order
            results.sort(Comparator.comparingDouble(PQAnalysisResult::getMaxPrice).reversed());

        }
        results.forEach(System.out::println);
        Set<Leg> orderLegs = new HashSet<>();
        // split till quantity is fulfilled
        for (PQAnalysisResult result : results) {
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
            String xid = orderAPICommHandler.placeOrder(PropertiesReader.getProperty(leg.getXchange() + "_BASE_URL"), body);
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

        Order processedOrder = savedOrder.orElse(null);
        if (processedOrder != null) {
            // TODO: publish and forget
            Payload reportPayload = new Payload();
            reportPayload.setOrder(processedOrder);
            reportPayload.setNumberOfLegs(processedOrder.getLegs().size());
            reportPayload.setLegs(processedOrder.getLegs());
            reportPayload.setPriceAnalysisResults(results);
            reportPayload.setTotalOrderValue(processedOrder.getValue());
            // Creating Object of ObjectMapper define in Jackson API
            ObjectMapper Obj = new ObjectMapper();
            try {
                // Converting the Java object into a JSON string
                String jsonStr = Obj.writeValueAsString(reportPayload);
                // Displaying Java object into a JSON string
                messagePublisher.publish(jsonStr);
                System.out.println(jsonStr);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return processedOrder;
    }

    public void battleStation(Order clientOrder) {
        clientOrder.setValue(clientOrder.getPrice() * clientOrder.getQuantity());
        // TODO: make a request to the market data service to get the current market price
        List<ExchangeDataPayload> prices;
        try {
            prices = Arrays.asList(marketDataAPICommHandler.getMarketDataByProduct(clientOrder.getProduct()));
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "prices could not be found for the product: " + clientOrder.getProduct() + " due to " + e.getMessage());
        }

        if (prices.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "prices could not be found for the product: " + clientOrder.getProduct());
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

        Side side = Side.valueOf(clientOrder.getSide().toUpperCase());
        Integer totalQuantity = clientOrder.getQuantity();

        // go scan the order book and return the result
        List<PQAnalysisResult> results = pqAnalyzer.analyze(xs, clientOrder.getProduct(), side);
        if (side == Side.BUY) {
            // sort in ascending order
            results.sort(Comparator.comparingDouble(PQAnalysisResult::getMinPrice));
        } else {
            // sort in descending order
            results.sort(Comparator.comparingDouble(PQAnalysisResult::getMaxPrice).reversed());

        }
        results.forEach(System.out::println);
        Set<Leg> orderLegs = new HashSet<>();
        // split till quantity is fulfilled
        for (PQAnalysisResult result : results) {
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
            String xid = orderAPICommHandler.placeOrder(PropertiesReader.getProperty(leg.getXchange() + "_BASE_URL"), body);
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

        Order processedOrder = savedOrder.orElse(null);
        if (processedOrder != null) {
            // TODO: publish and forget
            Payload reportPayload = new Payload();
            reportPayload.setOrder(processedOrder);
            reportPayload.setNumberOfLegs(processedOrder.getLegs().size());
            reportPayload.setLegs(processedOrder.getLegs());
            reportPayload.setPriceAnalysisResults(results);
            reportPayload.setTotalOrderValue(processedOrder.getValue());
            // Creating Object of ObjectMapper define in Jackson API
            ObjectMapper Obj = new ObjectMapper();
            try {
                // Converting the Java object into a JSON string
                String jsonStr = Obj.writeValueAsString(reportPayload);
                // Displaying Java object into a JSON string
                messagePublisher.publish(jsonStr);
                System.out.println(jsonStr);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public Order getOrderById(Long id) {
        Optional<Order> d = this.orderRepository.findById(id);
        if (d.isPresent()) {
            return d.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no order exists with id: " + id);
        }
    }

    public List<Leg> getOrderLegs(Long id) {
        return this.legRepository.findByOrderId(id);
    }

    public List<Order> listUserOrders(Long id) {
        return this.orderRepository.findByClientId(id);
    }

    @Scheduled(initialDelay = 100, fixedDelay = 3000)
    public void pollOrderStatuses() {
        List<Order> executingOrders = this.orderRepository.getExecutingOrders();
        executingOrders.forEach((order -> {
            List<Leg> orderLegs = this.legRepository.findByOrderId(order.getId());
            orderLegs.forEach(leg -> {
                try {
                    orderAPICommHandler.getOrderItemById(PropertiesReader.getProperty(leg.getXchange() + "_BASE_URL"), leg.getXid());
                } catch (Exception e) {
                    leg.setStatus(OrderStatus.FULFILLED);
                    // TODO: send fulfilled orders to Elorm
                }
            });
            if (orderLegs.stream().allMatch(x -> x.getStatus() == OrderStatus.FULFILLED)) {
                order.setStatus(OrderStatus.FULFILLED);
            }
            this.orderRepository.save(order);
            this.legRepository.saveAll(orderLegs);
        }));
    }

    @Scheduled(initialDelay = 100, fixedDelay = 1000)
    public void processDeferredOrders() {

    }

    @Scheduled(initialDelay = 20000, fixedDelay = 100)
    public void taylor() {
        List<String> products = List.of("MSFT", "NFLX", "GOOGL", "AAPL", "AMZN", "ORCL", "TSLA");
        products.forEach(product -> {
            Order d = new Order();
            d.setClientId(1L);
            d.setIsShort(false);
            d.setQuantity(100);
            d.setSide("BUY");
            d.setProduct(product);
            d.setPrice(1.5);
            this.battleStation(d);
        });

        products.forEach(product -> {
            Order d = new Order();
            d.setClientId(1L);
            d.setIsShort(false);
            d.setQuantity(100);
            d.setSide("SELL");
            d.setProduct(product);
            d.setPrice(0.1);
            this.battleStation(d);
        });
    }

    public Iterable<Order> listOrdersByStatusAndSide(OrderStatus status, String s) {
        return this.orderRepository.getOrdersByStatusAndSide(status, s);
    }

    public Iterable<Order> listOrdersByStatus(OrderStatus valueOf) {
        return this.orderRepository.getOrdersByStatus(valueOf);
    }

    public Iterable<Order> listOrdersBySide(String s) {
        return this.orderRepository.getOrdersBySide(s);
    }
}
