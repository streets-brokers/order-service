package com.streets.ordersvc.dao.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.streets.ordersvc.common.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clientId;
    private String product;
    private Boolean isShort;
    private Integer quantity;
    private Double value;
    private Long timestamp;
    private Long updatedAt;
    private String side;
    private Double marketPrice;
    private Double price;
    private OrderStatus status;
    private String xid;

    @JsonIgnore
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Leg> legs;

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
