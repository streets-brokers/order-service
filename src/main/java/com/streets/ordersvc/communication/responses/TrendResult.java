package com.streets.ordersvc.communication.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrendResult {

    // this captures the trend percentage
    private Integer direction;

    // this captures the exchange on which this trend was computed
    private String exchange;

    // this captures the value based on which the trend percentage was calculated
    private String valueType;
}

