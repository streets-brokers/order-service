package com.streets.ordersvc.processing.strategy.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TrendAnalysisResult {
    private String product;
    private String exchange;
    // for delta less than zero signals decreasing
    // for delta greater than zero signals increasing
    // for delta equal to zero signals no movement
    private Double delta;

    public TrendAnalysisResult() {
    }
}
