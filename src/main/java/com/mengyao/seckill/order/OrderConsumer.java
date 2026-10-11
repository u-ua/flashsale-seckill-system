package com.mengyao.seckill.order;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderConsumer {
    private final VoucherOrderRepository orders;
    public OrderConsumer(VoucherOrderRepository orders) { this.orders = orders; }
    @KafkaListener(topics = "${seckill.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(OrderMessage message) {
        if (orders.existsByUserIdAndVoucherId(message.userId(), message.voucherId())) return;
        try { orders.save(new VoucherOrder(message.orderId(), message.userId(), message.voucherId())); }
        catch (DataIntegrityViolationException ignored) { /* DB unique key is the final idempotency fence. */ }
    }
}
