package com.streets.ordersvc.communication.responses;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class OrderBookItem {
    @JsonAlias({"PRODUCT", "product"})
    private String product;
    @JsonAlias({"PRICE", "price"})
    private Double price;
    @JsonAlias({"SIDE", "side"})
    private String side;

    @JsonAlias({"QUANTITY", "quantity"})
    private Integer quantity;

    @JsonAlias({"CUMULATIVEQUANTITY", "cumulativeQuantity"})
    private Integer cumulativeQuantity;
    @JsonAlias({"EXECUTIONS", "executions"})
    private List<Execution> executions;

    public OrderBookItem(String product, Double price, String side, Integer quantity, Integer cumulativeQuantity, List<Execution> executions) {
        this.product = product;
        this.price = price;
        this.side = side;
        this.quantity = quantity;
        this.cumulativeQuantity = cumulativeQuantity;
        this.executions = executions;
    }

    public String getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Integer getCumulativeQuantity() {
        return cumulativeQuantity;
    }

    public void setCumulativeQuantity(Integer cumulativeQuantity) {
        this.cumulativeQuantity = cumulativeQuantity;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<Execution> executions) {
        this.executions = executions;
    }

}
