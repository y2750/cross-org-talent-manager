package com.crossorgtalentmanager.mapper;

import com.mybatisflex.core.BaseMapper;
import com.crossorgtalentmanager.model.entity.Department;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 记录公司部门及部门领导 映射层。
 *
 * 增加一个原生 SQL 方法用于在记录被逻辑删除时强制恢复（直接 update is_delete = 0）。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 直接将指定 id 的 is_delete 置为 0（恢复）。
     * 返回受影响的行数。
     */
    @Update("UPDATE department SET is_delete = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);

}
