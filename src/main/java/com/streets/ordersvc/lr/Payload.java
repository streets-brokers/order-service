package com.streets.ordersvc.lr;

import com.streets.ordersvc.common.dao.models.Leg;
import com.streets.ordersvc.common.dao.models.Order;
import com.streets.ordersvc.communication.responses.TrendResult;
import com.streets.ordersvc.processing.strategy.results.PQAnalysisResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Payload {
    private Integer numberOfLegs;
    private List<String> exchanges;
    private Double totalOrderValue;
    private Set<Leg> legs;
    private List<PQAnalysisResult> priceAnalysisResults;
    private List<TrendResult> trendResults;
    private Order order;
}
