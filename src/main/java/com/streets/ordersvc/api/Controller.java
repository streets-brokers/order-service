package com.streets.ordersvc.api;


import com.streets.ordersvc.api.requests.OrderRequestBody;
import com.streets.ordersvc.common.dao.models.Leg;
import com.streets.ordersvc.common.dao.models.Order;
import com.streets.ordersvc.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/v1/orderservice")
public class Controller {
    private final OrderService service;

    @Autowired
    public Controller(OrderService orderService) {
        this.service = orderService;
    }


    @PostMapping("/orders")
    @ResponseBody
    public Order orderPlacementHandler(@RequestBody OrderRequestBody request) {
        if (request.getUserId() == null || request.getUserId() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "user id must be supplied");
        }
        Long userId = request.getUserId();
        Order d = new Order();
        d.setQuantity(request.getQuantity());
        d.setPrice(request.getPrice());
        d.setClientId(request.getUserId());
        d.setSide(request.getSide());
        d.setProduct(request.getProduct());
        // TODO: no short selling for now
        d.setIsShort(false);
        try {
            return service.placeOrder(d);
        } catch (ResponseStatusException e) {
            throw e;
        }

    }

    @GetMapping("/{id}")
    @ResponseBody
    public Order getOrderHandler(@PathVariable Long id) {
        return this.service.getOrderById(id);

    }
    @GetMapping("/{id}/legs")
    @ResponseBody
    public List<Leg> getOrderLegsHandler(@PathVariable Long id) {
        return this.service.getOrderLegs(id);

    }

    @GetMapping("/users/{userId}")
    @ResponseBody
    public Iterable<Order> listUserOrders(@PathVariable Long userId) {
        return this.service.listUserOrders(userId);
    }
}
