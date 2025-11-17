package com.crossorgtalentmanager.service;

import com.crossorgtalentmanager.model.dto.user.UserQueryRequest;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.crossorgtalentmanager.model.dto.employee.EmployeeQueryRequest;
import com.crossorgtalentmanager.model.entity.Employee;
import com.crossorgtalentmanager.model.vo.EmployeeVO;

import java.util.List;

/**
 * 员工基本信息 服务层。
 *
 * @author <a href="https://github.com/y2750">y</a>
 */
public interface EmployeeService extends IService<Employee> {

    long employeeCreate(Long companyId, String name, String gender, String phone, String email, String idCardNumber,
            Long departmentId);

    /**
     * 转换 Employee 实体为 EmployeeVO（包装类）。
     */
    EmployeeVO getEmployeeVO(Employee employee);

    /**
     * 批量转换 Employee 列表为 EmployeeVO 列表。
     */
    List<EmployeeVO> getEmployeeVOList(List<Employee> employeeList);

    /**
     * 根据查询条件构建 QueryWrapper。
     */
    QueryWrapper getQueryWrapper(EmployeeQueryRequest employeeQueryRequest);

    /**
     * 根据请求与调用者用户分页查询员工 VO 列表。
     * 管理员查看所有员工，HR 或公司管理员查看本公司员工。
     */
    com.mybatisflex.core.paginate.Page<com.crossorgtalentmanager.model.vo.EmployeeVO> pageEmployeeVOByPage(
            EmployeeQueryRequest employeeQueryRequest, com.crossorgtalentmanager.model.entity.User loginUser);
}
