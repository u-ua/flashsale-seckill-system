package com.mengyao.seckill.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("voucher");
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(5)));
        return manager;
    }
}
