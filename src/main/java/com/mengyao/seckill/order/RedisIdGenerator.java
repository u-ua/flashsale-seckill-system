package com.mengyao.seckill.order;

import java.time.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisIdGenerator {
    private static final long BEGIN_TIMESTAMP = LocalDateTime.of(2025, 1, 1).toEpochSecond(ZoneOffset.UTC);
    private final StringRedisTemplate redis;
    public RedisIdGenerator(StringRedisTemplate redis) { this.redis = redis; }
    public long nextId(String prefix) {
        long seconds = Instant.now().getEpochSecond() - BEGIN_TIMESTAMP;
        long sequence = redis.opsForValue().increment("icr:" + prefix + ":" + LocalDate.now()) & 0xffffffffL;
        return (seconds << 32) | sequence;
    }
}
