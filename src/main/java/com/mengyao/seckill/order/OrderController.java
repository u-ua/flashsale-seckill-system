package com.mengyao.seckill.order;

import com.mengyao.seckill.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final VoucherOrderRepository orders;
    public OrderController(VoucherOrderRepository orders) { this.orders = orders; }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderStatus> status(@PathVariable long orderId) {
        return orders.findById(orderId)
            .map(order -> ApiResponse.ok(new OrderStatus(orderId, "PERSISTED", order.getUserId(), order.getVoucherId())))
            .orElseGet(() -> ApiResponse.ok(new OrderStatus(orderId, "PROCESSING", null, null)));
    }
}
