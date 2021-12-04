package com.streets.ordersvc.communication.requests;


import com.fasterxml.jackson.annotation.JsonAlias;

public class OrderRequestBody {
    @JsonAlias({"product"})
    private String product;
    @JsonAlias({"quantity"})
    private Integer quantity;
    @JsonAlias({"price"})
    private Double price;
    @JsonAlias({"side"})
    private String side;

    public OrderRequestBody(String product, Integer quantity, Double price, String side) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
    }

    @Override
    public String toString() {
        return "OrderRequestBody{" +
                "product='" + product + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", side='" + side + '\'' +
                '}' + '\n';
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
}
