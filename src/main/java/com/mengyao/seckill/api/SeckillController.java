package com.mengyao.seckill.api;

import com.mengyao.seckill.order.SeckillService;
import com.mengyao.seckill.ratelimit.RateLimit;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
public class SeckillController {
    private final SeckillService service;
    public SeckillController(SeckillService service) { this.service = service; }

    @PostMapping("/{voucherId}/orders")
    @RateLimit
    public ApiResponse<OrderAccepted> order(@PathVariable @Positive long voucherId,
                                             @RequestHeader("X-User-Id") @Positive long userId) {
        return ApiResponse.ok(service.submit(voucherId, userId));
    }
}
