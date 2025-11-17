package com.crossorgtalentmanager.mapper;

import com.mybatisflex.core.BaseMapper;
import com.crossorgtalentmanager.model.entity.EmployeeProfile;

/**
 * 员工档案信息 映射层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface EmployeeProfileMapper extends BaseMapper<EmployeeProfile> {
    // mapper 保持简单，不提供恢复已逻辑删除数据的方法（逻辑删除仅切换 isDelete）
}
