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
                        Long departmentId, boolean isAdmin);

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
                        EmployeeQueryRequest employeeQueryRequest,
                        com.crossorgtalentmanager.model.entity.User loginUser);

        /**
         * 根据身份证号推断性别（第17位，奇数为男性，偶数为女性）
         * 
         * @param idCardNumber 身份证号（15位或18位）
         * @return 性别（"男"或"女"），如果无法推断则返回null
         */
        String inferGenderFromIdCard(String idCardNumber);

        /**
         * 确认解雇员工：将companyId和departmentId置为null，status置为false
         * 使用事务处理，如果失败则回滚
         * 
         * @param employeeId 员工ID
         * @param loginUser  登录用户
         * @return 是否成功
         */
        Boolean confirmFireEmployee(Long employeeId, com.crossorgtalentmanager.model.entity.User loginUser);

        /**
         * 批量移出部门：将指定员工的departmentId置为null
         * 使用原生SQL确保null值能够被正确设置
         * 
         * @param employeeIds 员工ID列表
         * @param loginUser   登录用户
         * @return 成功移出的员工数量
         */
        int batchRemoveFromDepartment(java.util.List<Long> employeeIds,
                        com.crossorgtalentmanager.model.entity.User loginUser);

        /**
         * 批量添加到部门：将指定员工的departmentId设置为指定部门ID
         * 使用原生SQL确保批量更新能够正确执行
         * 
         * @param employeeIds  员工ID列表
         * @param departmentId 部门ID
         * @param loginUser    登录用户
         * @return 成功添加的员工数量
         */
        int batchAddToDepartment(java.util.List<Long> employeeIds, Long departmentId,
                        com.crossorgtalentmanager.model.entity.User loginUser);

        /**
         * 批量导入员工（从Excel文件）
         * 
         * @param file       Excel文件
         * @param companyId  公司ID
         * @param loginUser  登录用户
         * @return 导入结果
         */
        com.crossorgtalentmanager.model.dto.employee.EmployeeBatchImportResult batchImportEmployees(
                        org.springframework.web.multipart.MultipartFile file,
                        Long companyId,
                        com.crossorgtalentmanager.model.entity.User loginUser);
}
