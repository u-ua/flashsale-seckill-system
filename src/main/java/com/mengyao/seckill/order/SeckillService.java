package com.mengyao.seckill.order;

import com.mengyao.seckill.api.OrderAccepted;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillService {
    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> script;
    private final RedisIdGenerator ids;
    private final KafkaTemplate<String, OrderMessage> kafka;
    private final String topic;
    public SeckillService(StringRedisTemplate redis, DefaultRedisScript<Long> script, RedisIdGenerator ids,
                          KafkaTemplate<String, OrderMessage> kafka, @Value("${seckill.kafka-topic}") String topic) {
        this.redis = redis; this.script = script; this.ids = ids; this.kafka = kafka; this.topic = topic;
    }
    public OrderAccepted submit(long voucherId, long userId) {
        Long result = redis.execute(script, List.of(stockKey(voucherId), userSetKey(voucherId)), Long.toString(userId));
        if (result == null) throw new SeckillException("秒杀服务暂不可用");
        if (result == 1) throw new SeckillException("库存不足");
        if (result == 2) throw new SeckillException("请勿重复下单");
        long orderId = ids.nextId("order");
        kafka.send(topic, Long.toString(voucherId), new OrderMessage(orderId, userId, voucherId));
        return new OrderAccepted(orderId, "ACCEPTED");
    }
    public static String stockKey(long voucherId) { return "seckill:stock:" + voucherId; }
    public static String userSetKey(long voucherId) { return "seckill:order:users:" + voucherId; }
}
