package com.streets.ordersvc.api.requests;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestBody {
    @JsonAlias({"product"})
    private String product;
    @JsonAlias({"quantity"})
    private Integer quantity;
    @JsonAlias({"price"})
    private Double price;
    @JsonAlias({"side"})
    private String side;

    @JsonAlias({"userId", "user_id"})
    private String userId;

}
