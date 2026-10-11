package com.mengyao.seckill.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Hot read cache: Caffeine L1 + Redis L2. A Redis lock suppresses concurrent cache rebuilding. */
@Service
public class VoucherCacheService {
    private final StringRedisTemplate redis;
    private final RedissonClient redisson;
    private final ObjectMapper mapper;
    public VoucherCacheService(StringRedisTemplate redis, RedissonClient redisson, ObjectMapper mapper) { this.redis=redis; this.redisson=redisson; this.mapper=mapper; }

    @Cacheable(cacheNames="voucher", key="#voucherId", unless="#result == null")
    public String get(long voucherId, Supplier<String> databaseQuery) {
        String key = "cache:voucher:" + voucherId;
        String cached = redis.opsForValue().get(key);
        if (cached != null) return "__NULL__".equals(cached) ? null : cached; // caches empty values to resist penetration
        RLock lock = redisson.getLock("lock:voucher:" + voucherId);
        try {
            if (!lock.tryLock(100, 5_000, TimeUnit.MILLISECONDS)) return null;
            cached = redis.opsForValue().get(key);
            if (cached != null) return "__NULL__".equals(cached) ? null : cached;
            String result = databaseQuery.get();
            redis.opsForValue().set(key, result == null ? "__NULL__" : result, result == null ? Duration.ofMinutes(2) : Duration.ofMinutes(30));
            return result;
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); return null; }
        finally { if (lock.isHeldByCurrentThread()) lock.unlock(); }
    }
    public void warm(long voucherId, Object value) {
        try { redis.opsForValue().set("cache:voucher:" + voucherId, mapper.writeValueAsString(value), Duration.ofMinutes(30)); }
        catch (JsonProcessingException e) { throw new IllegalArgumentException("缓存序列化失败", e); }
    }
    public void seedStock(long voucherId, int stock) {
        if (stock < 0) throw new IllegalArgumentException("库存不能为负数");
        redis.opsForValue().set("seckill:stock:" + voucherId, Integer.toString(stock));
        redis.delete("seckill:order:users:" + voucherId);
    }
}
