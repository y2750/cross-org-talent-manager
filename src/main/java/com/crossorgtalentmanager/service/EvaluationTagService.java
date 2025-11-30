package com.crossorgtalentmanager.service;

import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.entity.EvaluationTag;
import com.crossorgtalentmanager.model.vo.EvaluationTagVO;

import java.util.List;

/**
 * 评价标签库 服务层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface EvaluationTagService extends IService<EvaluationTag> {

    /**
     * 获取所有启用的评价标签列表
     * @return 标签列表
     */
    List<EvaluationTagVO> listActiveTags();

    /**
     * 转换 EvaluationTag 实体为 EvaluationTagVO
     */
    EvaluationTagVO getEvaluationTagVO(EvaluationTag tag);

    /**
     * 批量转换 EvaluationTag 列表为 EvaluationTagVO 列表
     */
    List<EvaluationTagVO> getEvaluationTagVOList(List<EvaluationTag> tagList);
}

