package com.mengyao.seckill.order;

public record OrderStatus(long orderId, String status, Long userId, Long voucherId) { }
