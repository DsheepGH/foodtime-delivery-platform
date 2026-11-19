package com.foodtime.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 令牌工具类，用于防止重复提交（接口幂等性）
 * 基于用户ID的自动Token模式
 */
@Component
@Slf4j
public class TokenUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    // 防重复提交前缀（基于用户ID）
    private static final String SUBMIT_PREFIX = "order:submit:user:";

    // 防重复提交时间窗口（秒）
    private static final long SUBMIT_WINDOW_SECONDS = 30; // 30秒内不能重复提交
    /**
     * 后端自动校验（基于用户ID，前端无感知）
     * 使用SETNX原子操作，确保同一用户在指定时间窗口内只能提交一次
     *
     * @param userId 用户ID
     * @return true-允许提交（首次提交），false-拒绝提交（重复提交）
     */
    public boolean validateAndConsumeToken(Long userId) {
        if (userId == null) {
            return false;
        }
        String key = SUBMIT_PREFIX + userId;

        // SETNX原子操作：如果key不存在则设置成功，返回true；如果key已存在则返回false
        Boolean result = redisTemplate.opsForValue().setIfAbsent(
                key, "1", SUBMIT_WINDOW_SECONDS, TimeUnit.SECONDS);

        log.info("用户{}提交校验结果：{}", userId, result);
        return result != null && result;
    }

    /**
     * 主动删除用户的提交记录（用于下单失败后允许重试）
     *
     * @param userId 用户ID
     */
    public void removeSubmitRecord(Long userId) {
        if (userId != null) {
            String key = SUBMIT_PREFIX + userId;
            redisTemplate.delete(key);
            log.info("清除用户{}的提交记录", userId);
        }
    }
}
