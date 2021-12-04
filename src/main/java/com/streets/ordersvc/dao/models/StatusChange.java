package com.streets.ordersvc.dao.models;

import com.streets.ordersvc.common.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StatusChange {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private Long orderId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private Long timestamp;
}
