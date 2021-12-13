package com.streets.ordersvc.processing.strategy.analyzers;

import com.streets.ordersvc.common.enums.Side;
import com.streets.ordersvc.communication.internal.mds.MarketDataAPICommHandler;
import com.streets.ordersvc.communication.outbound.OrderAPICommHandler;
import com.streets.ordersvc.communication.responses.FullOrderBook;
import com.streets.ordersvc.communication.responses.OrderBookItem;
import com.streets.ordersvc.processing.strategy.results.PQAnalysisResult;
import com.streets.ordersvc.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

@Component
// This is for scanning price and quantity of the full order book
public class PQAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PQAnalyzer.class);
    private final OrderAPICommHandler commHandler;
    @Autowired
    private MarketDataAPICommHandler marketDataAPICommHandler;

    private final ConcurrentHashMap<String, List<PQAnalysisResult>> cachedOpenBidsAnalysisResults = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<PQAnalysisResult>> cachedOpenAsksAnalysisResults = new ConcurrentHashMap<>();

    private final String[] xs = {"EXCHANGE1", "EXCHANGE2"};

    @Autowired
    public PQAnalyzer(OrderAPICommHandler commHandler) {
        this.commHandler = commHandler;
    }

    public List<PQAnalysisResult> analyze(String[] exchanges, String product, Side side) {
        if (side == Side.BUY) {
            return this.cachedOpenAsksAnalysisResults.get(product);
        } else {
            return this.cachedOpenBidsAnalysisResults.get(product);
        }
    }

    private List<PQAnalysisResult> analyzeOpenBids(String[] exchanges, String product) {
        List<PQAnalysisResult> scans = new ArrayList<>();
        for (String exchange : exchanges) {
            String baseURL = PropertiesReader.getProperty(exchange + "_BASE_URL");
            FullOrderBook book = commHandler.getBuyOpenedOrderBookByProduct(baseURL, product);

            PQAnalysisResult pqAnalysisResult = new PQAnalysisResult();
            pqAnalysisResult.setSide(Side.BUY.toString());
            pqAnalysisResult.setExchange(exchange);
            pqAnalysisResult.setProduct(product);

            pqAnalysisResult.setQuantity(calculateAvailableQuantity(book));

            // gets the max price the product is selling at
            OptionalDouble maxp = maxPrice(book);
            if (maxp.isPresent()) {
                pqAnalysisResult.setMaxPrice(maxp.getAsDouble());
            }else{
                pqAnalysisResult.setMaxPrice((double) 0);
            }

            OptionalDouble minp = minPrice(book);
            if (minp.isPresent()) {
                pqAnalysisResult.setMinPrice(minp.getAsDouble());
            }else{
                pqAnalysisResult.setMinPrice((double) 0);
            }

            scans.add(pqAnalysisResult);
        }

        return scans;
    }

    private List<PQAnalysisResult> analyzeOpenAsks(String[] exchanges, String product) {
        List<PQAnalysisResult> scans = new ArrayList<>();
        for (String exchange : exchanges) {
            String baseURL = PropertiesReader.getProperty(exchange + "_BASE_URL");
            FullOrderBook book = commHandler.getSellOpenedOrderBookByProduct(baseURL, product);

            PQAnalysisResult pqAnalysisResult = new PQAnalysisResult();
            pqAnalysisResult.setSide(Side.SELL.toString());
            pqAnalysisResult.setExchange(exchange);
            pqAnalysisResult.setProduct(product);
            pqAnalysisResult.setQuantity(calculateAvailableQuantity(book));

            // gets the max price the product is selling at
            OptionalDouble maxp = maxPrice(book);
            if (maxp.isPresent()) {
                pqAnalysisResult.setMaxPrice(maxp.getAsDouble());
            }

            OptionalDouble minp = minPrice(book);
            if (minp.isPresent()) {
                pqAnalysisResult.setMinPrice(minp.getAsDouble());
            }
            scans.add(pqAnalysisResult);
        }
        return scans;
    }

    /**
     * quantityAvailable = ∑(quantity) - ∑(cumulativeQuantity)
     */
    private Integer calculateAvailableQuantity(FullOrderBook book) {
        Integer totalCumulativeQuantity = book.getFullOrderBook().stream().mapToInt(OrderBookItem::getCumulativeQuantity).sum();
        Integer totalQuantity = book.getFullOrderBook().stream().mapToInt(OrderBookItem::getQuantity).sum();
        return totalQuantity - totalCumulativeQuantity;
    }

    private OptionalDouble minPrice(FullOrderBook book) {
        return book.getFullOrderBook().stream().mapToDouble(OrderBookItem::getPrice).min();

    }

    private OptionalDouble maxPrice(FullOrderBook book) {
        return book.getFullOrderBook().stream().mapToDouble(OrderBookItem::getPrice).max();
    }

    @Scheduled(initialDelay = 100, fixedDelay = 1000)
    public void loadFullOrderBook() {
        LOGGER.info("Running PQ Analysis");
        List<String> products = List.of(marketDataAPICommHandler.getMarketProducts());
        for (String product : products) {
            LOGGER.info(product);
            this.cachedOpenBidsAnalysisResults.put(product, this.analyzeOpenBids(xs, product));
            this.cachedOpenAsksAnalysisResults.put(product, this.analyzeOpenAsks(xs, product));
        }
        LOGGER.info("PQ Analysis finished");
    }
}
