package com.crossorgtalentmanager.ai;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: AiTalentComparisonServiceFactory
 * Package: com.crossorgtalentmanager.ai.model.message
 * Description:
 *
 * @Author
 * @Create 2025/12/3 18:13
 * @Version 1.0
 */

/**
 * AI 人才比较服务工厂
 */
@Configuration
@Slf4j
public class AiTalentComparisonServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;
    
}
