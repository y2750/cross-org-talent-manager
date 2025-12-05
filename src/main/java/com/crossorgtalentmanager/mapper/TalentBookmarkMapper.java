package com.crossorgtalentmanager.mapper;

import com.crossorgtalentmanager.model.entity.TalentBookmark;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 人才收藏 Mapper接口
 *
 * @author y
 */
@Mapper
public interface TalentBookmarkMapper extends BaseMapper<TalentBookmark> {
    
    /**
     * 查询收藏记录（包括已删除的），用于检查唯一约束
     * 注意：使用 ResultMap 或显式字段映射，确保 is_delete 字段被正确映射
     */
    @Select("SELECT id, company_id, employee_id, remark, create_time, update_time, is_delete " +
            "FROM talent_bookmark WHERE company_id = #{companyId} AND employee_id = #{employeeId} LIMIT 1")
    @org.apache.ibatis.annotations.Results({
            @org.apache.ibatis.annotations.Result(property = "id", column = "id"),
            @org.apache.ibatis.annotations.Result(property = "companyId", column = "company_id"),
            @org.apache.ibatis.annotations.Result(property = "employeeId", column = "employee_id"),
            @org.apache.ibatis.annotations.Result(property = "remark", column = "remark"),
            @org.apache.ibatis.annotations.Result(property = "createTime", column = "create_time"),
            @org.apache.ibatis.annotations.Result(property = "updateTime", column = "update_time"),
            @org.apache.ibatis.annotations.Result(property = "isDelete", column = "is_delete", 
                    javaType = Boolean.class, typeHandler = org.apache.ibatis.type.BooleanTypeHandler.class)
    })
    TalentBookmark selectOneIncludingDeleted(@Param("companyId") Long companyId, @Param("employeeId") Long employeeId);
    
    /**
     * 恢复已删除的收藏记录
     */
    @Update("UPDATE talent_bookmark SET is_delete = 0, remark = #{remark}, update_time = NOW() WHERE company_id = #{companyId} AND employee_id = #{employeeId}")
    int restoreBookmark(@Param("companyId") Long companyId, @Param("employeeId") Long employeeId, @Param("remark") String remark);
}


