package com.streets.ordersvc.communication.responses;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Execution {
    @JsonAlias({"TIMESTAMP", "timestamp"})
    private String timestamp;
    @JsonAlias({"PRICE", "price"})
    private Double price;
    @JsonAlias({"QUANTITY", "quantity"})
    private Integer quantity;

    public Execution(String timestamp, Double price, Integer quantity) {
        this.timestamp = timestamp;
        this.price = price;
        this.quantity = quantity;

    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }


}
