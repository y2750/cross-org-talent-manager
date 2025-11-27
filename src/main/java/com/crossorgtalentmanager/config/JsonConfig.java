package com.crossorgtalentmanager.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * Spring MVC Json 配置
 * 解决 Long 类型精度丢失问题
 */
@Configuration
@JsonComponent
public class JsonConfig {

    /**
     * 添加 Long 转 json 精度丢失的配置
     * 将 Long 类型序列化为 String，避免 JavaScript 精度丢失
     * 同时支持将 String 反序列化为 Long，确保请求参数中的字符串ID能正确转换
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();

        // Long 类型序列化为字符串（输出到前端）
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // Long 类型反序列化：支持从字符串转换为 Long（接收前端请求）
        module.addDeserializer(Long.class, new JsonDeserializer<Long>() {
            @Override
            public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                    throws IOException {
                String value = jsonParser.getText();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new IOException("无法将字符串转换为 Long: " + value, e);
                }
            }
        });

        // Long.TYPE (long 基本类型) 的反序列化
        module.addDeserializer(Long.TYPE, new JsonDeserializer<Long>() {
            @Override
            public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                    throws IOException {
                String value = jsonParser.getText();
                if (value == null || value.trim().isEmpty()) {
                    return 0L;
                }
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new IOException("无法将字符串转换为 long: " + value, e);
                }
            }
        });

        objectMapper.registerModule(module);
        return objectMapper;
    }
}
