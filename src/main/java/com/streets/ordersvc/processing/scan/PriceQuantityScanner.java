package com.streets.ordersvc.processing.scan;

import com.streets.ordersvc.common.enums.Side;
import com.streets.ordersvc.communication.outbound.OrderAPICommHandler;
import com.streets.ordersvc.communication.responses.FullOrderBook;
import com.streets.ordersvc.communication.responses.OrderBookItem;
import com.streets.ordersvc.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Service
// This is for scanning price and quantity of the full order book
public class PriceQuantityScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceQuantityScanner.class);

    public List<ScanResult> scanBook(String[] exchangeNames, String product, Side side) {
        if (side == Side.BUY) {
            return scanSells(exchangeNames, product);
        } else {
            return scanBuys(exchangeNames, product);
        }
    }

    private List<ScanResult> scanBuys(String[] exchangeNames, String product) {
        List<ScanResult> scans = new ArrayList<>();

        // TODO:(romeo) parallelize the shit outta this
        for (String exchangeName : exchangeNames) {
            String baseURL = PropertiesReader.getProperty(exchangeName + "_BASE_URL");
            FullOrderBook book = OrderAPICommHandler.getBuyOpenedOrderBookByProduct(baseURL, product);

            ScanResult scanResult = new ScanResult();
            scanResult.setSide(Side.BUY.toString());
            scanResult.setExchange(exchangeName);
            scanResult.setProduct(product);

            scanResult.setQuantity(calculateAvailableQuantity(book));

            // gets the max price the product is selling at
            OptionalDouble maxp = maxPrice(book);
            if (maxp.isPresent()) {
                scanResult.setMaxPrice(maxp.getAsDouble());
            }

            OptionalDouble minp = minPrice(book);
            if (minp.isPresent()) {
                scanResult.setMinPrice(minp.getAsDouble());
            }

            scans.add(scanResult);
        }

        return scans;
    }

    private List<ScanResult> scanSells(String[] exchangeNames, String product) {
        List<ScanResult> scans = new ArrayList<>();

        // TODO:(romeo) parallelize the shit outta this
        for (String exchangeName : exchangeNames) {
            String baseURL = PropertiesReader.getProperty(exchangeName + "_BASE_URL");
            FullOrderBook book = OrderAPICommHandler.getSellOpenedOrderBookByProduct(baseURL, product);

            ScanResult scanResult = new ScanResult();
            scanResult.setSide(Side.SELL.toString());
            scanResult.setExchange(exchangeName);
            scanResult.setProduct(product);
            scanResult.setQuantity(calculateAvailableQuantity(book));

            // gets the max price the product is selling at
            OptionalDouble maxp = maxPrice(book);
            if (maxp.isPresent()) {
                scanResult.setMaxPrice(maxp.getAsDouble());
            }

            OptionalDouble minp = minPrice(book);
            if (minp.isPresent()) {
                scanResult.setMinPrice(minp.getAsDouble());
            }

            scans.add(scanResult);
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
}
