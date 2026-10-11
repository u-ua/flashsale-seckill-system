package com.mengyao.seckill.order;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SeckillContractTest {
    @Test void usesStableRedisKeys() {
        assertEquals("seckill:stock:9", SeckillService.stockKey(9));
        assertEquals("seckill:order:users:9", SeckillService.userSetKey(9));
    }
    @Test void luaChecksStockBeforeMarkingUser() throws Exception {
        String lua = new String(new ClassPathResource("lua/seckill.lua").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(lua.indexOf("stock <= 0") < lua.indexOf("SADD"));
        assertTrue(lua.contains("SISMEMBER"));
        assertTrue(lua.contains("DECR"));
    }
}
