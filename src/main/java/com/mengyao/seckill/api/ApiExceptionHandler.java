package com.mengyao.seckill.api;

import com.mengyao.seckill.order.SeckillException;
import com.mengyao.seckill.ratelimit.RateLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(SeckillException.class)
    ResponseEntity<ApiResponse<Void>> seckill(SeckillException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
    }
    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ApiResponse<Void>> limited(RateLimitExceededException e) {
        return ResponseEntity.status(429).body(ApiResponse.fail(e.getMessage()));
    }
}
