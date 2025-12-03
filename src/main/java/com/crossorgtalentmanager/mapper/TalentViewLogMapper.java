package com.crossorgtalentmanager.mapper;

import com.crossorgtalentmanager.model.entity.TalentViewLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 人才浏览记录 Mapper接口
 *
 * @author y
 */
@Mapper
public interface TalentViewLogMapper extends BaseMapper<TalentViewLog> {

    /**
     * 查询浏览记录（包括已逻辑删除的记录）
     * 用于处理唯一键冲突时恢复已删除的记录
     *
     * @param companyId  企业ID
     * @param employeeId 员工ID
     * @return 浏览记录
     */
    @Select("SELECT * FROM talent_view_log WHERE company_id = #{companyId} AND employee_id = #{employeeId} LIMIT 1")
    TalentViewLog selectOneIncludingDeleted(@Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId);
}

