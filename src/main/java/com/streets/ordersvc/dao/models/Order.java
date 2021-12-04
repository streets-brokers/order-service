package com.streets.ordersvc.dao.models;

import com.streets.ordersvc.common.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private Long clientId;
    private String product;
    private Boolean isShort;
    private Integer quantity;
    private Double value;
    private Long timestamp;
    private String side;
    private Double marketPrice;
    private Double price;
    private OrderStatus status;
    private String xid;

    @OneToMany(mappedBy = "leg", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Leg> legs;
}
