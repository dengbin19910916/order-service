package io.xxx.orderservice.site.web;

import io.xxx.orderservice.domain.Order;
import io.xxx.orderservice.domain.OrderMessage;
import io.xxx.orderservice.site.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping("/create")
    public OrderMessage create(@Validated @RequestBody Order order) {
        return orderService.create(order);
    }

}
