package com.crossorgtalentmanager.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.dto.department.DepartmentQueryRequest;
import com.crossorgtalentmanager.model.entity.Department;
import com.crossorgtalentmanager.model.vo.DepartmentVO;
import java.util.List;

/**
 * 记录公司部门及部门领导 服务层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface DepartmentService extends IService<Department> {

    Long addDepartment(String name, Long companyId, Long leaderId);

    /**
     * 转换 Department 实体为 DepartmentVO（包装类）。
     */
    DepartmentVO getDepartmentVO(Department department);

    /**
     * 批量转换 Department 列表为 DepartmentVO 列表。
     */
    List<DepartmentVO> getDepartmentVOList(List<Department> departmentList);

    /**
     * 根据查询条件构建 QueryWrapper。
     */
    QueryWrapper getQueryWrapper(DepartmentQueryRequest departmentQueryRequest);

    /**
     * 切换部门的删除状态（逻辑删除或恢复）。
     */
    Boolean removeById(Long id);
}
