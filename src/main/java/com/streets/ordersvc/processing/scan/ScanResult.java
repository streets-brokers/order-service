package com.streets.ordersvc.processing.scan;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanResult {
    public ScanResult(String exchange, String product, Double maxPrice, Double minPrice, Integer quantity, String side) {
        this.exchange = exchange;
        this.product = product;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.quantity = quantity;
        this.side = side;
    }

    public ScanResult() {}


    private String exchange;
    private String product;
    private Double maxPrice;
    private Double minPrice;
    private Integer quantity;
    private String side;

}
