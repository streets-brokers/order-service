package com.streets.ordersvc.processing.strategy.analyzers;

import com.streets.ordersvc.common.enums.Side;
import com.streets.ordersvc.communication.outbound.OrderAPICommHandler;
import com.streets.ordersvc.processing.strategy.results.TrendAnalysisResult;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;



@Component
public class TrendAnalyzer {

    public TrendAnalyzer() {}

    // To know the price movement, it makes sense to examine
    // the executions and the market data for the particular side
    // Examine the prices over 5 mins interval
    // Compute the rate of change
    // Have a threshold for which
    public List<TrendAnalysisResult> analyze(String[] exchanges, String product, Side side) {
        return null;
    }
}
