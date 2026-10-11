package com.mengyao.seckill.ratelimit;

import java.lang.annotation.*;
@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit { }
