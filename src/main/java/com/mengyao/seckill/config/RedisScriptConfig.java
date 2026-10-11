package com.mengyao.seckill.config;

import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.*;

@Configuration
public class RedisScriptConfig {
    @Bean
    DefaultRedisScript<Long> seckillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill.lua")); script.setResultType(Long.class);
        return script;
    }
}
