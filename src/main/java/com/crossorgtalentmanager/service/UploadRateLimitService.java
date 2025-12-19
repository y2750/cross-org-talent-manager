package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 上传限流服务
 * 实现：1分钟内上传超过10张图片，限制3分钟不能上传
 */
@Service
@Slf4j
public class UploadRateLimitService {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 1分钟内最多允许上传的图片数量
     */
    private static final int MAX_UPLOADS_PER_MINUTE = 10;

    /**
     * 超过限制后的禁止时间（秒）
     */
    private static final int BAN_DURATION_SECONDS = 180; // 3分钟

    /**
     * 时间窗口（秒）
     */
    private static final int TIME_WINDOW_SECONDS = 60; // 1分钟

    /**
     * 检查是否可以上传图片
     * 
     * @param identifier 标识符（IP地址或用户ID）
     * @param uploadCount 本次要上传的图片数量
     * @throws BusinessException 如果被限流则抛出异常
     */
    public void checkUploadLimit(String identifier, int uploadCount) {
        if (identifier == null || identifier.isEmpty()) {
            identifier = "unknown";
        }

        String banKey = "upload:ban:" + identifier;
        String countKey = "upload:count:" + identifier;

        // 检查是否在禁止期内
        RBucket<Long> banBucket = redissonClient.getBucket(banKey);
        if (banBucket.isExists()) {
            long banUntil = banBucket.get();
            long now = System.currentTimeMillis();
            if (now < banUntil) {
                long remainingSeconds = (banUntil - now) / 1000;
                throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,
                        String.format("上传过于频繁，请在 %d 秒后再试", remainingSeconds));
            }
            // 禁止期已过，清除ban标记
            banBucket.delete();
        }

        // 检查当前时间窗口内的上传数量
        RBucket<Integer> countBucket = redissonClient.getBucket(countKey);
        int currentCount = countBucket.get() == null ? 0 : countBucket.get();

        // 如果本次上传后超过限制，设置禁止期
        if (currentCount + uploadCount > MAX_UPLOADS_PER_MINUTE) {
            long banUntil = System.currentTimeMillis() + BAN_DURATION_SECONDS * 1000L;
            banBucket.set(banUntil);
            banBucket.expire(BAN_DURATION_SECONDS, TimeUnit.SECONDS);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,
                    String.format("1分钟内上传图片数量不能超过 %d 张，您已上传 %d 张。请在 %d 秒后再试",
                            MAX_UPLOADS_PER_MINUTE, currentCount, BAN_DURATION_SECONDS));
        }

        // 更新计数
        if (currentCount == 0) {
            // 第一次上传，设置过期时间
            countBucket.set(uploadCount);
            countBucket.expire(TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
        } else {
            // 增加计数，保持原有的过期时间
            countBucket.set(currentCount + uploadCount);
        }

        log.debug("上传限流检查通过：identifier={}, currentCount={}, uploadCount={}, total={}",
                identifier, currentCount, uploadCount, currentCount + uploadCount);
    }

    /**
     * 获取客户端IP地址（用于未登录用户的限流）
     */
    public String getClientIP(jakarta.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

