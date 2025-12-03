package com.crossorgtalentmanager.mapper;

import com.crossorgtalentmanager.model.entity.Complaint;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投诉 Mapper 接口
 */
@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {
}
