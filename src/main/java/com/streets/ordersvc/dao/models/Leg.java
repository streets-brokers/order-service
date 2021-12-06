package com.streets.ordersvc.dao.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.streets.ordersvc.common.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "legs")
@Getter
@Setter
public class Leg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String product;
    private Integer quantity;
    private Double value;
    private Long timestamp;
    private Long updatedAt;
    private String side;
    private Double marketPrice;
    private Double price;
    private OrderStatus status;
    private String xid;
    private String xchange;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


    @PreUpdate
    public void preUpdate() {
        Date now = new Date();
        updatedAt = now.getTime();
        value = price * quantity;
    }

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        timestamp = now.getTime();
        updatedAt = now.getTime();
        value = price * quantity;
    }
}
