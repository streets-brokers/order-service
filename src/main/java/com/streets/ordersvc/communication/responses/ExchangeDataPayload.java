package com.streets.ordersvc.communication.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
public class ExchangeDataPayload {

    @JsonAlias({"ID", "id"})
    private Long id;

    @JsonAlias({"TICKER", "ticker"})
    private String ticker;

    @JsonAlias({"XCHANGE", "xchange"})
    private String xchange;

    @JsonAlias({"TIMESTAMP", "timestamp"})
    private Long timestamp;

    @JsonAlias({"SELL_LIMIT", "sellLimit"})
    private Integer sellLimit;

    @JsonAlias({"MAX_PRICE_SHIFT", "maxPriceShift"})
    private Double maxPriceShift;

    @JsonAlias({"ASK_PRICE", "askPrice"})
    private Double askPrice;

    @JsonAlias({"BID_PRICE", "bidPrice"})
    private Double bidPrice;

    @JsonAlias({"BUY_LIMIT", "buyLimit"})
    private Integer buyLimit;

    @JsonAlias({"LAST_TRADED_PRICE", "lastTradedPrice"})
    private Double lastTradedPrice;

    public ExchangeDataPayload(Long id, String ticker, String xchange, Long timestamp, Integer sellLimit, Double maxPriceShift, Double askPrice, Double bidPrice, Integer buyLimit, Double lastTradedPrice) {
        this.id = id;
        this.ticker = ticker;
        this.xchange = xchange;
        this.timestamp = timestamp;
        this.sellLimit = sellLimit;
        this.maxPriceShift = maxPriceShift;
        this.askPrice = askPrice;
        this.bidPrice = bidPrice;
        this.buyLimit = buyLimit;
        this.lastTradedPrice = lastTradedPrice;
    }

    public ExchangeDataPayload() {
    }


    @Override
    public String toString() {
        return "RawXchangeData{" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                ", xchange='" + xchange + '\'' +
                ", timestamp=" + timestamp +
                ", sellLimit=" + sellLimit +
                ", maxPriceShift=" + maxPriceShift +
                ", askPrice=" + askPrice +
                ", bidPrice=" + bidPrice +
                ", buyLimit=" + buyLimit +
                '}' + '\n';
    }
}

