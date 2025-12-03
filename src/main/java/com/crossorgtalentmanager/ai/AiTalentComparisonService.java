package com.crossorgtalentmanager.ai;

import dev.langchain4j.service.SystemMessage;

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
     * 比较两个候选人的简历与评价和标签详情，并给出推荐意见
     *
     * @param resume1 第一个候选人的简历与评价和标签详情文本
     * @param resume2 第二个候选人的简历与评价和标签详情文本
     * @return 推荐意见字符串
     */
    @SystemMessage(fromResource = "prompt/talent-comparison-system-prompt.txt")
    String compare(String resume1, String resume2);
}
