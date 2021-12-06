package com.streets.ordersvc.communication.responses;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class FullOrderBook {
    @JsonAlias({"FULLORDERBOOK", "fullOrderBook"})
    private List<OrderBookItem> fullOrderBook;

    public FullOrderBook() {
    }

    public FullOrderBook(List<OrderBookItem> fullOrderBook) {
        this.fullOrderBook = fullOrderBook;
    }

    public List<OrderBookItem> getFullOrderBook() {
        return fullOrderBook;
    }

    public void setFullOrderBook(List<OrderBookItem> fullOrderBook) {
        this.fullOrderBook = fullOrderBook;
    }
}
