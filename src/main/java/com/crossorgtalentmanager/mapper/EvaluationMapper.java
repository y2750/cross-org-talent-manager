package com.crossorgtalentmanager.mapper;

import com.mybatisflex.core.BaseMapper;
import com.crossorgtalentmanager.model.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价记录 映射层
 */
@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {
    // 可以添加自定义查询方法
}
