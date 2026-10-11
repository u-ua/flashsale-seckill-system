package com.mengyao.seckill.order;

public record OrderMessage(long orderId, long userId, long voucherId) { }
