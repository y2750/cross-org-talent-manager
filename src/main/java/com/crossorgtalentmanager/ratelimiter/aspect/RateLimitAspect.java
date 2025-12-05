package com.crossorgtalentmanager.ratelimiter.aspect;

import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.ratelimiter.annotation.RateLimit;
import com.crossorgtalentmanager.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * ClassName: RateLimitAspect
 * Package: com.yy.yaicodemother.ratelimiter.aspect
 * Description:
 *
 * @Author
 * @Create 2025/10/18 08:35
 * @Version 1.0
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;

    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        Object[] args = point.getArgs();

        // 提取任务ID（如果存在）
        String taskId = extractTaskIdFromArgs(args, request);

        // 第一层：基于任务ID的限流（如果存在任务ID，用于轮询场景）
        if (taskId != null && !taskId.isEmpty()) {
            String taskKey = generateTaskBasedRateLimitKey(point, rateLimit, taskId, request);
            RRateLimiter taskRateLimiter = redissonClient.getRateLimiter(taskKey);
            // 每个任务的轮询：60秒内120次（支持正常轮询，每秒2次）
            boolean isNew = taskRateLimiter.trySetRate(RateType.OVERALL, 120, 60, RateIntervalUnit.SECONDS);
            if (isNew) {
                taskRateLimiter.expire(Duration.ofHours(1));
            }
            if (!taskRateLimiter.tryAcquire(1)) {
                log.warn("任务级限流触发：taskId={}, key={}", taskId, taskKey);
                throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
            }
        }

        // 第二层：基于用户的总体限流（防止恶意使用多个任务ID攻击）
        // 任务提交和轮询使用不同的限流key，避免互相影响
        if (taskId != null && !taskId.isEmpty()) {
            // 轮询场景：使用独立的用户级限流key（与任务提交分离）
            String pollingKey = generateUserBasedRateLimitKey(point, rateLimit, request) + ":polling";
            RRateLimiter pollingRateLimiter = redissonClient.getRateLimiter(pollingKey);
            // 轮询场景：用户级别使用更宽松的限流
            // 考虑：单个任务每秒轮询1次，最多轮询120秒（2分钟），单个任务最多120次
            // 如果用户同时有多个任务在轮询，或者历史任务累积，需要更大的限制
            // 设置为10000次/120秒，可以支持约80个任务同时轮询（80 * 120 = 9600次）
            // 使用120秒窗口而不是60秒，可以更好地处理历史请求的累积
            // 这个限制足够宽松，可以支持正常使用，同时也能防止恶意攻击
            int pollingRate = 10000; // 轮询场景：用户级别120秒内最多10000次（防止恶意使用多个任务ID）
            int pollingInterval = 120; // 使用120秒窗口，避免60秒窗口内累积过多请求

            boolean isNewPolling = pollingRateLimiter.trySetRate(RateType.OVERALL, pollingRate, pollingInterval,
                    RateIntervalUnit.SECONDS);
            if (isNewPolling) {
                pollingRateLimiter.expire(Duration.ofHours(1));
            }
            if (!pollingRateLimiter.tryAcquire(1)) {
                log.warn("用户级限流触发（轮询）：taskId={}, key={}, rate={}/{}", taskId, pollingKey, pollingRate, pollingInterval);
                throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
            }
        } else {
            // 任务提交场景：使用独立的用户级限流key（与轮询分离）
            String submitKey = generateUserBasedRateLimitKey(point, rateLimit, request) + ":submit";
            RRateLimiter submitRateLimiter = redissonClient.getRateLimiter(submitKey);
            // 任务提交等场景：使用注解配置的严格限流
            int submitRate = rateLimit.rate();
            int submitInterval = rateLimit.rateInterval();

            boolean isNewSubmit = submitRateLimiter.trySetRate(RateType.OVERALL, submitRate, submitInterval,
                    RateIntervalUnit.SECONDS);
            if (isNewSubmit) {
                submitRateLimiter.expire(Duration.ofHours(1));
            }
            if (!submitRateLimiter.tryAcquire(1)) {
                log.warn("用户级限流触发（任务提交）：key={}, rate={}/{}", submitKey, submitRate, submitInterval);
                throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
            }
        }
    }

    /**
     * 生成基于任务ID的限流key（用于轮询场景）
     */
    private String generateTaskBasedRateLimitKey(JoinPoint point, RateLimit rateLimit, String taskId,
            HttpServletRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:task:");
        // 添加自定义前缀
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        }

        // 根据限流类型生成不同的key
        switch (rateLimit.limitType()) {
            case USER:
                try {
                    if (request != null) {
                        User loginUser = userService.getLoginUser(request);
                        keyBuilder.append("user:").append(loginUser.getId()).append(":task:").append(taskId);
                    } else {
                        keyBuilder.append("ip:").append(getClientIP()).append(":task:").append(taskId);
                    }
                } catch (BusinessException e) {
                    keyBuilder.append("ip:").append(getClientIP()).append(":task:").append(taskId);
                }
                break;
            case IP:
                keyBuilder.append("ip:").append(getClientIP()).append(":task:").append(taskId);
                break;
            case API:
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName()).append(":task:").append(taskId);
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    /**
     * 生成基于用户的总体限流key（防止恶意使用多个任务ID攻击）
     */
    private String generateUserBasedRateLimitKey(JoinPoint point, RateLimit rateLimit, HttpServletRequest request) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:user_total:");
        // 添加自定义前缀
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        }

        // 根据限流类型生成不同的key
        switch (rateLimit.limitType()) {
            case API:
                // 接口级别：方法名
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName());
                break;
            case USER:
                // 用户级别：用户ID（总体限流，不包含任务ID）
                try {
                    if (request != null) {
                        User loginUser = userService.getLoginUser(request);
                        keyBuilder.append("user:").append(loginUser.getId());
                    } else {
                        keyBuilder.append("ip:").append(getClientIP());
                    }
                } catch (BusinessException e) {
                    keyBuilder.append("ip:").append(getClientIP());
                }
                break;
            case IP:
                // IP级别：客户端IP
                keyBuilder.append("ip:").append(getClientIP());
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    /**
     * 从方法参数或请求参数中提取任务ID
     * 用于区分不同AI任务的轮询请求
     */
    private String extractTaskIdFromArgs(Object[] args, HttpServletRequest request) {
        // 优先从请求参数中获取（@RequestParam String taskId）
        if (request != null) {
            String taskId = request.getParameter("taskId");
            if (taskId != null && !taskId.isEmpty()) {
                return taskId;
            }
        }
        // 从方法参数中获取（@RequestParam String taskId 通常是第一个参数）
        if (args != null && args.length > 0) {
            Object firstArg = args[0];
            if (firstArg instanceof String) {
                String str = (String) firstArg;
                // 检查是否是任务ID格式（通常包含下划线或特定前缀，长度较长）
                if (str.startsWith("ai_task_") || (str.length() > 20 && str.contains("_"))) {
                    return str;
                }
            }
            // 遍历所有参数查找任务ID
            for (Object arg : args) {
                if (arg instanceof String) {
                    String str = (String) arg;
                    if (str.startsWith("ai_task_") || (str.length() > 20 && str.contains("_"))) {
                        return str;
                    }
                }
            }
        }
        return null;
    }

    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

}
