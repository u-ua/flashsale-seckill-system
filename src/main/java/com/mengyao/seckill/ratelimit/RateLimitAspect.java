package com.mengyao.seckill.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimitAspect {
    private final SlidingWindowRateLimiter limiter;
    private final HttpServletRequest request;
    private final int userLimit, ipLimit, globalLimit;
    public RateLimitAspect(SlidingWindowRateLimiter limiter, HttpServletRequest request,
        @Value("${seckill.rate-limit.user}") int userLimit, @Value("${seckill.rate-limit.ip}") int ipLimit,
        @Value("${seckill.rate-limit.global}") int globalLimit) {
        this.limiter=limiter; this.request=request; this.userLimit=userLimit; this.ipLimit=ipLimit; this.globalLimit=globalLimit;
    }
    @Before("@annotation(com.mengyao.seckill.ratelimit.RateLimit)")
    public void check() {
        String user = request.getHeader("X-User-Id");
        String ip = request.getRemoteAddr();
        boolean accepted = limiter.allow("global", globalLimit, 1_000)
            && limiter.allow("ip:" + ip, ipLimit, 1_000)
            && limiter.allow("user:" + (user == null ? "anonymous" : user), userLimit, 1_000);
        if (!accepted) throw new RateLimitExceededException();
    }
}
