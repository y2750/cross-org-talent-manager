package com.crossorgtalentmanager.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * AI 人才比较服务工厂
 * 负责创建和配置AI人才对比服务
 *
 * @author y
 */
@Configuration
@Slf4j
public class AiTalentComparisonServiceFactory {

    /**
     * 创建AI人才对比服务Bean
     * 使用默认的 chatModel（由 langchain4j-spring-boot-starter 自动配置）
     * 如果有多个 ChatModel，优先使用 chatModel，如果没有则使用第一个可用的
     *
     * @param chatModels 所有 ChatModel 实例（自动注入）
     * @return AI人才对比服务实例
     */
    @Bean
    public AiTalentComparisonService createAiTalentComparisonService(
            @Autowired(required = false) List<ChatModel> chatModels) {
        log.info("开始创建AI人才对比服务，找到 {} 个 ChatModel Bean",
                chatModels != null ? chatModels.size() : 0);

        ChatModel chatModel = null;
        if (chatModels != null && !chatModels.isEmpty()) {
            // 使用第一个 ChatModel（通常是默认的 chat-model）
            chatModel = chatModels.get(0);
            log.info("使用 ChatModel: {}", chatModel.getClass().getSimpleName());
        }

        if (chatModel == null) {
            log.error("未找到 ChatModel Bean，无法创建AI人才对比服务");
            throw new IllegalStateException("ChatModel未配置，请检查 langchain4j.open-ai.chat-model 配置");
        }

        try {
            AiTalentComparisonService service = AiServices.builder(AiTalentComparisonService.class)
                    .chatModel(chatModel)
                    .build();
            log.info("AI人才对比服务创建成功");
            return service;
        } catch (Exception e) {
            log.error("创建AI人才对比服务失败", e);
            throw new IllegalStateException("创建AI人才对比服务失败: " + e.getMessage(), e);
        }
    }
}
