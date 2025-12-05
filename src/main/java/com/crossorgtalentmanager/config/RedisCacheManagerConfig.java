package com.crossorgtalentmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCacheManagerConfig {

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
                // 配置 ObjectMapper 支持 Java8 时间类型
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                // GenericJackson2JsonRedisSerializer 默认会在 JSON 中添加 @class 字段保存类型信息
                // 这样可以正确反序列化泛型类型（如 List<T>）和复杂对象（如 CompanyPreferenceVO）
                // 注意：不要使用 activateDefaultTyping，因为会改变序列化格式，导致与旧数据不兼容
                GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(
                                objectMapper);

                // 默认配置
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)) // 默认 30 分钟过期
                                .disableCachingNullValues() // 禁用 null 值缓存
                                // key 使用 String 序列化器
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                // value 使用 JSON 序列化器（支持复杂对象和泛型）
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(jsonSerializer));

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(defaultConfig)
                                // 评价标签列表缓存 - 静态配置数据，很少变化，设置1小时过期
                                .withCacheConfiguration("evaluationTags",
                                                defaultConfig.entryTtl(Duration.ofHours(1)))
                                // 解锁价格配置缓存 - 静态配置数据，很少变化，设置1小时过期
                                .withCacheConfiguration("unlockPriceConfigs",
                                                defaultConfig.entryTtl(Duration.ofHours(1)))
                                // 企业招聘偏好缓存 - 按公司ID，相对稳定，设置30分钟过期
                                .withCacheConfiguration("companyPreference",
                                                defaultConfig.entryTtl(Duration.ofMinutes(30)))
                                .build();
        }
}
