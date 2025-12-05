package com.crossorgtalentmanager.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * ClassName: AiTalentComparisonservice
 * Package: com.crossorgtalentmanager.ai.model.message
 * Description:
 *
 * @Author
 * @Create 2025/12/3 18:10
 * @Version 1.0
 */
public interface AiTalentComparisonService {
    /**
     * 对比多个候选人的简历、评价和标签详情，并给出推荐意见
     *
     * @param inputData 完整的输入数据（包含公司偏好和候选人信息）
     * @return AI分析结果（JSON格式字符串）
     */
    @SystemMessage(fromResource = "prompt/talent-comparison-system-prompt.txt")
    String compareTalents(@UserMessage String inputData);
}

