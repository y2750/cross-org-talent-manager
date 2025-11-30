package com.crossorgtalentmanager.mapper;

import com.mybatisflex.core.BaseMapper;
import com.crossorgtalentmanager.model.entity.EvaluationTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价任务表 映射层
 */
@Mapper
public interface EvaluationTaskMapper extends BaseMapper<EvaluationTask> {
}
