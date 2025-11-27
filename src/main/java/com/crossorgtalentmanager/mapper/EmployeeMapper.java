package com.crossorgtalentmanager.mapper;

import com.mybatisflex.core.BaseMapper;
import com.crossorgtalentmanager.model.entity.Employee;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 员工基本信息 映射层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 解雇员工：将companyId和departmentId置为null，status置为false
     */
    @Update("UPDATE employee SET company_id = NULL, department_id = NULL, status = 0 WHERE id = #{id}")
    int fireEmployee(@Param("id") Long id);

    /**
     * 批量移出部门：将指定员工的departmentId置为null
     * 使用原生SQL确保null值能够被正确设置
     */
    @Update("<script>" +
            "UPDATE employee SET department_id = NULL WHERE id IN " +
            "<foreach collection='employeeIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchRemoveFromDepartment(@Param("employeeIds") List<Long> employeeIds);

}
