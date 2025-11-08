package com.crossorgtalentmanager.mapper;

import com.mybatisflex.core.BaseMapper;
import com.crossorgtalentmanager.model.entity.Company;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 企业信息管理 映射层。
 *
 * 增加一个原生 SQL 方法用于在记录被逻辑删除时强制恢复（直接 update is_delete = 0），
 * 因为框架的逻辑删除机制可能会在更新/查询时自动附加过滤条件，导致无法修改已被逻辑删除的行。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface CompanyMapper extends BaseMapper<Company> {

    /**
     * 直接将指定 id 的 is_delete 置为 0（恢复）。
     * 返回受影响的行数。
     */
    @Update("UPDATE company SET is_delete = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);

}
