package com.mengyao.seckill.catalog;

import com.mengyao.seckill.api.ApiResponse;
import com.mengyao.seckill.cache.VoucherCacheService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
public class VoucherController {
    private final VoucherRepository vouchers;
    private final VoucherCacheService cache;
    public VoucherController(VoucherRepository vouchers, VoucherCacheService cache) { this.vouchers=vouchers; this.cache=cache; }
    @GetMapping("/{voucherId}")
    public ApiResponse<String> detail(@PathVariable long voucherId) {
        return ApiResponse.ok(cache.get(voucherId, () -> vouchers.findById(voucherId)
            .map(v -> String.format("{\"id\":%d,\"title\":\"%s\",\"stock\":%d}", v.getId(), v.getTitle(), v.getStock())).orElse(null)));
    }
    @PostMapping("/{voucherId}/stock")
    public ApiResponse<Map<String, Object>> seedStock(@PathVariable long voucherId, @RequestParam int stock) {
        cache.seedStock(voucherId, stock);
        return ApiResponse.ok(Map.of("voucherId", voucherId, "stock", stock));
    }
}
