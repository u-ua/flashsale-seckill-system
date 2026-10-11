package com.mengyao.seckill.ratelimit;
public class RateLimitExceededException extends RuntimeException { public RateLimitExceededException() { super("请求过于频繁，请稍后再试"); } }
