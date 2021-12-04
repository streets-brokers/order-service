package com.streets.ordersvc.dao.models;

import com.streets.ordersvc.common.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Leg {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private String product;
    private Integer quantity;
    private Double value;
    private Long timestamp;
    private String side;
    private Double marketPrice;
    private Double price;
    private OrderStatus status;
    private String xid;
    private String xchange;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
