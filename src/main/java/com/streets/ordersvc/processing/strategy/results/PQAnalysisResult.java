package com.streets.ordersvc.processing.strategy.results;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PQAnalysisResult {
    public PQAnalysisResult(String exchange, String product, Double maxPrice, Double minPrice, Integer quantity, String side) {
        this.exchange = exchange;
        this.product = product;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.quantity = quantity;
        this.side = side;
    }

    public PQAnalysisResult() {}

    @Override
    public String toString() {
        return "ScanResult{" +
                "exchange='" + exchange + '\'' +
                ", product='" + product + '\'' +
                ", maxPrice=" + maxPrice +
                ", minPrice=" + minPrice +
                ", quantity=" + quantity +
                ", side='" + side + '\'' +
                '}' + '\n';
    }

    private String exchange;
    private String product;
    private Double maxPrice;
    private Double minPrice;
    private Integer quantity;
    private String side;

}
