package com.crossorgtalentmanager.mapper;

import com.mybatisflex.core.BaseMapper;
import com.crossorgtalentmanager.model.entity.EvaluationTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价标签关联表 映射层
 */
@Mapper
public interface EvaluationTagRelationMapper extends BaseMapper<EvaluationTagRelation> {
}
