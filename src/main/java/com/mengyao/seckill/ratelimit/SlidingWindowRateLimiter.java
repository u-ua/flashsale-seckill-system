package com.mengyao.seckill.ratelimit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/** One Redis Lua execution makes the ZSet trim/count/add/expire sequence atomic. */
@Component
public class SlidingWindowRateLimiter {
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(
        "local now=tonumber(ARGV[1]); local window=tonumber(ARGV[2]); local limit=tonumber(ARGV[3]); " +
        "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now-window); " +
        "if redis.call('ZCARD', KEYS[1]) >= limit then return 0 end; " +
        "redis.call('ZADD', KEYS[1], now, ARGV[4]); redis.call('PEXPIRE', KEYS[1], window); return 1", Long.class);
    private final StringRedisTemplate redis;
    public SlidingWindowRateLimiter(StringRedisTemplate redis) { this.redis = redis; }
    public boolean allow(String dimension, int limit, long windowMillis) {
        Long allowed = redis.execute(SCRIPT, List.of("rate:" + dimension), Long.toString(System.currentTimeMillis()),
            Long.toString(windowMillis), Integer.toString(limit), UUID.randomUUID().toString());
        return Long.valueOf(1).equals(allowed);
    }
}
