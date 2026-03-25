package com.jiaotou.bigmodel.util;

import com.jiaotou.bigmodel.common.constant.Constants;
import com.jiaotou.bigmodel.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的滑动窗口限流
 * 每用户每分钟最多 10 次 AI 请求
 */
@Component
@RequiredArgsConstructor
public class RateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public void check(String userId) {
        String key = Constants.REDIS_RATE_LIMIT + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) return;
        if (count == 1) {
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }
        if (count > Constants.RATE_LIMIT_PER_MINUTE) {
            throw new BusinessException(429, "请求过于频繁，每分钟最多" +
                    Constants.RATE_LIMIT_PER_MINUTE + "次，请稍后再试");
        }
    }
}
